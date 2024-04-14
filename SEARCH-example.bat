@ECHO OFF
java -Dlog4j.configurationFile=log4j2_windows.xml -jar target\IndexerAndSearchTool-0.0.2-SNAPSHOT-jar-with-dependencies.jar --mode search --indexpath %cd%/documents/index --outpath %cd%/documents/output --numberofresults 50 --query "tüv"
PAUSE