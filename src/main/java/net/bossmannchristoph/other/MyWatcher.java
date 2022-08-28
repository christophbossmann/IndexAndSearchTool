package net.bossmannchristoph.other;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class MyWatcher {

	@SuppressWarnings("unused")
	private void printPaths(final Path root) throws IOException {
		// register all subfolders
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path p, BasicFileAttributes bfa) throws IOException {
				return super.visitFile(p, bfa);				
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				try {
					System.out.println(dir.toString());
					return super.preVisitDirectory(dir, attrs);
				} catch (Exception e) {
					System.out.println("error on: " + dir + " " + e.getClass());
					return FileVisitResult.SKIP_SUBTREE;
				}
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				try {
					return super.postVisitDirectory(dir, exc);
				} catch (Exception e) {
					System.out.println("error on: " + dir + " " + exc.getClass());
					return FileVisitResult.SKIP_SUBTREE;
				}
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				System.out.println("error on: " + file + " " + exc.getClass());
				return FileVisitResult.SKIP_SUBTREE;
			}
		});
	}
	
	private Map<Path, WatchKey> registerPathRecursive(Path root, final WatchService watchService, final WatchEvent.Kind<?>... standardWatchEventKinds) throws IOException {
		final Map<Path, WatchKey> watchKeys = new HashMap<>();
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path p, BasicFileAttributes bfa) throws IOException {
				return super.visitFile(p, bfa);				
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				try {
					return super.preVisitDirectory(dir, attrs);
				} catch (Exception e) {
					System.out.println("error on: " + dir + " " + e.getClass());
					return FileVisitResult.SKIP_SUBTREE;
				}
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				try {
					if(exc != null) {
						System.out.println("io error on: " + dir + " " + exc.getClass() + ", " + exc.getMessage());
					}
					WatchKey watchKey = dir.register(watchService, standardWatchEventKinds);
					watchKeys.put(dir, watchKey);
					return super.postVisitDirectory(dir, exc);
				} catch (Exception e) {
					System.out.println("error on: " + dir + " " + e.getMessage());
					return FileVisitResult.SKIP_SUBTREE;
				}
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				System.out.println("error on: " + file + " " + exc.getClass());
				return FileVisitResult.SKIP_SUBTREE;
			}
		});
		return watchKeys;
	}

	public static void main(String[] args) {
		try {
			MyWatcher watcher = new MyWatcher();
			//watcher.handleFileEventsLoop(Paths.get("D:\\Office\\Arbeit\\ISS SopraSteria\\jiraandbitbucketreportgenerator\\reportgenerator\\"));
			//watcher.handleFileEventsLoop(Paths.get("C:\\Users\\Christoph\\Desktop"));
			watcher.handleFileEventsLoop(Paths.get("E:\\Fotos"));
			//new Watcher().printPaths(Paths.get("D:\\Office"));
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleFileEventsLoop(Path root) throws IOException, InterruptedException {
		WatchKey key;
		WatchService watchService = FileSystems.getDefault().newWatchService();
		registerPathRecursive(root, 
				watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
		
		while ((key = watchService.take()) != null) {
		    for (WatchEvent<?> event : key.pollEvents()) {
		    	System.out.println(
		                  "Event kind:" + event.kind() 
		                    + ". File affected: " + event.context() + ".");
		    }
		    key.reset();
		}
	}
}
