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

package ru.iris.common.database.model;

import com.google.gson.annotations.Expose;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "datasources")
public class DataSource
{

	@Expose
	@Id
	private int id;

	// type (google-cal, etc)
	@Expose
	private String type;

	@Expose
	private String title;

	// JSON object
	@Expose
	private String obj;

	// Активна ли?
	@Expose
	private boolean enabled;

	public DataSource()
	{
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getObj()
	{
		return obj;
	}

	public void setObj(String object)
	{
		this.obj = object;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}