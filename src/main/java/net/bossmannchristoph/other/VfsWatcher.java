package net.bossmannchristoph.other;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

public class VfsWatcher {

	public void initVfs(FileListener fileListener) throws FileSystemException, InterruptedException {
		FileSystemManager fsManager = VFS.getManager();
		FileObject listendir = fsManager.resolveFile("D:\\Office");
		DefaultFileMonitor fm = new DefaultFileMonitor(fileListener);
		 fm.setRecursive(true);
		 fm.addFile(listendir);
		 fm.start();
		 while(true) {
			 Thread.sleep(1000);
		 }
	}
	
	public void init() throws FileSystemException, InterruptedException {
		FileListener fileListener = new FileListener() {
			
			@Override
			public void fileDeleted(FileChangeEvent event) throws Exception {
				System.out.println("File deleted");
				
			}
			
			@Override
			public void fileCreated(FileChangeEvent event) throws Exception {
				System.out.println("File created!");
				
			}
			
			@Override
			public void fileChanged(FileChangeEvent event) throws Exception {
				System.out.println("File changed!");
				
			}
		};
		initVfs(fileListener);
	}
	
	public static void main(String[] args) throws FileSystemException, InterruptedException {
		VfsWatcher vfsWatcher = new VfsWatcher();
		vfsWatcher.init();
	}
	
}
