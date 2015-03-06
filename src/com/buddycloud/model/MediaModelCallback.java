package com.buddycloud.model;

/**
 * This class used to keep track the progress for up-streaming
 * media files.
 * 
 * @author Adnan Urooj (Deminem)
 */
public interface MediaModelCallback<T> extends ModelCallback<T> {
	
	/**
	 * This method keep tracking the progress for up-streaming
	 * files.
	 * 
	 * @param progress
	 * @param totalSize
	 */
	void progress(long progress, long totalSize);
}
