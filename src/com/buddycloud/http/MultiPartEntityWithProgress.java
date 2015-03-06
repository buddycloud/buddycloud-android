package com.buddycloud.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

/**
 * This class used to handle the multi-part entity post with streaming progress.
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class MultiPartEntityWithProgress extends MultipartEntity {

	private ProgressListener listener;

	public MultiPartEntityWithProgress(final ProgressListener listener) {
		super();
		this.listener = listener;
	}

	public MultiPartEntityWithProgress(final HttpMultipartMode mode,
			final ProgressListener listener) {
		super(mode);
		this.listener = listener;
	}

	public MultiPartEntityWithProgress(HttpMultipartMode mode,
			final String boundary, final Charset charset,
			final ProgressListener listener) {
		super(mode, boundary, charset);
		this.listener = listener;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.listener, this.getContentLength()));
	}

	/**
	 * ProgressListener interface which keep track of the data transfered on
	 * server.
	 *
	 */
	public static interface ProgressListener {
		void transferred(long progress, long totalSize);
	}

	/**
	 * Counting the number of bytes transfered through HTTP stream and update
	 * the progress listener delegate method.
	 *
	 */
	public static class CountingOutputStream extends FilterOutputStream {

		private final ProgressListener listener;
		private long totalSize;
		private long transferred;

		public CountingOutputStream(final OutputStream out,
				final ProgressListener listener, final long totalSize) {
			super(out);
			this.listener = listener;
			this.totalSize = totalSize;
			this.transferred = 0;
		}

		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			this.transferred += len;
			this.listener.transferred(this.transferred, this.totalSize);
		}

		public void write(int b) throws IOException {
			out.write(b);
			this.transferred++;
			this.listener.transferred(this.transferred, this.totalSize);
		}
	}
}
