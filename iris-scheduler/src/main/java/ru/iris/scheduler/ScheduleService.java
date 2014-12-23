/*
 * Copyright 2012-2014 Nikolay A. Viguro
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.iris.scheduler;

import com.avaje.ebean.Ebean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import ru.iris.common.database.model.DataSource;
import ru.iris.common.database.model.Task;
import ru.iris.common.messaging.JsonEnvelope;
import ru.iris.common.messaging.JsonMessaging;
import ru.iris.common.messaging.JsonNotification;
import ru.iris.common.messaging.model.tasks.TaskChangesAdvertisement;
import ru.iris.common.messaging.model.tasks.TaskSourcesChangesAdvertisement;
import ru.iris.common.messaging.model.tasks.TasksStartAdvertisement;
import ru.iris.common.messaging.model.tasks.TasksStopAdvertisement;
import ru.iris.common.source.googlecal.GoogleCalendarSource;
import ru.iris.common.source.vk.VKSource;
import ru.iris.scheduler.jobs.SendCommandAdvertisementJob;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ScheduleService
{
	private final Logger LOGGER = LogManager.getLogger(ScheduleService.class);
	private List<Task> events = null;
	private List<DataSource> sources = null;
	private Scheduler scheduler = null;

	public ScheduleService()
	{
		try {
			SchedulerFactory factory = new StdSchedulerFactory();
			scheduler = factory.getScheduler();
		} catch (SchedulerException e) {
			LOGGER.error("Error: " + e.getMessage());
		}

			// run
			readSources();
			readAndScheduleTasks();

			final JsonMessaging jsonMessaging = new JsonMessaging(UUID.randomUUID(), "events");
			jsonMessaging.subscribe("event.scheduler.reload.tasks");
			jsonMessaging.subscribe("event.scheduler.reload.sources");
			jsonMessaging.subscribe("event.scheduler.stop");
			jsonMessaging.subscribe("event.scheduler.start");
			jsonMessaging.subscribe("event.scheduler.restart");

		jsonMessaging.setNotification(new JsonNotification() {
			@Override
			public void onNotification(JsonEnvelope envelope) {

				try {

					if (envelope.getObject() instanceof TasksStartAdvertisement) {
						LOGGER.info("Start/restart scheduler service!");
						// reload events
						events = null;
						readAndScheduleTasks();
					} else if (envelope.getObject() instanceof TasksStopAdvertisement) {
						LOGGER.info("Stop scheduler service");
						// reload events
						scheduler.shutdown();
						events = null;
					} else if (envelope.getObject() instanceof TaskSourcesChangesAdvertisement) {
						LOGGER.info("Reload sources list");

						// reload sources
						sources = null;
						sources = Ebean.find(DataSource.class).where().eq("enabled", true).findList();

						// take pause to save/remove new entity
						Thread.sleep(500);
						readSources();

						LOGGER.info("Loaded " + sources.size() + " sources.");
					} else if (envelope.getObject() instanceof TaskChangesAdvertisement) {
						LOGGER.info("Reload tasks list");
						readAndScheduleTasks();
					}

					} catch (Exception e) {
					LOGGER.error("Error: " + e.getMessage());
				}
				}
		});

		jsonMessaging.start();
	}

	private void readSources()
	{
		sources = Ebean.find(DataSource.class).where().eq("enabled", true).findList();

		for (DataSource source : sources)
		{
			switch (source.getType()) {
				// google calendar
				case "gcal":
					GoogleCalendarSource.getInstance().populateCalendar(source.getObj());
					break;
				// VK.com
				case "vk":
					VKSource.getInstance().populateBirthDayCalendar(source.getObj());
					break;
				default:
					LOGGER.info("Unknown data source: " + source.getType() + "!");
					break;
			}
		}
	}

	private void readAndScheduleTasks()
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);

		events = null;

		events = Ebean.find(Task.class)
				.where()
				.eq("enabled", true)
				.between("startdate", new Date(), cal.getTime())
				.findList();

		// take pause to save/remove new entity
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try
		{
			//Start scheduler
			if (!scheduler.isStarted())
			{
				LOGGER.info("Scheduling tasks!");
				scheduler.start();
			}
			else
			{
				LOGGER.info("Rescheduling tasks!");

				// cancel all jobs
				for (JobKey job : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("scheduler"))) {
					LOGGER.debug("Interrupt and delete task: " + job.getName());
					scheduler.interrupt(job);
					scheduler.deleteJob(job);
				}

			}

			for (Task event : events)
			{
				JobDetailImpl jobDetail = new JobDetailImpl();
				jobDetail.setName(event.getTitle());
				jobDetail.setJobClass(SendCommandAdvertisementJob.class);
				jobDetail.setGroup("scheduler");

				jobDetail.getJobDataMap().put("subject", event.getSubject());
				jobDetail.getJobDataMap().put("obj", event.getObj());

				//Creating schedule time with trigger
				SimpleTriggerImpl trigger = new SimpleTriggerImpl();

				trigger.setStartTime(event.getStartdate());
				trigger.setEndTime(event.getEnddate());

				if (event.getPeriod() != null && !event.getPeriod().isEmpty())
					trigger.setRepeatInterval(Long.valueOf(event.getPeriod()));

				trigger.setName("trigger-" + event.getTitle());

				scheduler.scheduleJob(jobDetail, trigger);
			}

			LOGGER.info("Scheduled " + scheduler.getJobKeys(GroupMatcher.jobGroupEquals("scheduler")).size() + " tasks!");

		}
		catch (SchedulerException e)
		{
			LOGGER.error("Scheduler error: ", e);
		}
	}
}