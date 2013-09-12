package io.openkit;

public class OKURLBuilder {

	private String baseURL;
	private String fullURL;

	public OKURLBuilder(String baseURL)
	{
		if(baseURL.endsWith("/")) {
			this.baseURL = baseURL;
		} else {
			this.baseURL = baseURL + "/";
		}

		fullURL = this.baseURL;
	}

	public void appendPathComponent(String pathComponent)
	{
		if(pathComponent.startsWith("/")) {
			pathComponent = pathComponent.substring(1);
		}

		if(this.fullURL.endsWith("/")) {
			this.fullURL = this.fullURL + pathComponent;
		} else {
			this.fullURL = this.fullURL + "/" + pathComponent;
		}
	}

	public String build()
	{
		return this.fullURL;
	}
}
