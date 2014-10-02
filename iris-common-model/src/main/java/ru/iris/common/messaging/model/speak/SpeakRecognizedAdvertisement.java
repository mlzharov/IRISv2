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

package ru.iris.common.messaging.model.speak;

import com.google.gson.annotations.Expose;
import ru.iris.common.messaging.model.Advertisement;

public class SpeakRecognizedAdvertisement extends Advertisement
{
	/**
	 * Text to speak
	 */
	@Expose
	private String text;

	/**
	 * Confidence
	 */
	@Expose
	private double confidence;

	/**
	 * Terminal
	 */
	@Expose
	private String device;

	public SpeakRecognizedAdvertisement set(String text, double confidence)
	{
		this.text = text;
		this.confidence = confidence;
		this.device = "all";
		return this;
	}

	public SpeakRecognizedAdvertisement set(String text, double confidence, String device)
	{
		this.text = text;
		this.confidence = confidence;
		this.device = device;
		return this;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public double getConfidence()
	{
		return confidence;
	}

	public void setConfidence(double confidence)
	{
		this.confidence = confidence;
	}

	public String getDevice()
	{
		return device;
	}

	public void setDevice(String device)
	{
		this.device = device;
	}

	@Override
	public String toString()
	{
		return "SpeakRecognizedAdvertisement{" +
				"text='" + text + '\'' +
				", confidence=" + confidence +
				", device='" + device + '\'' +
				'}';
	}
}