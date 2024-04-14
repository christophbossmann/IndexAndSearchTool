@ECHO OFF
java -Dlog4j.configurationFile=log4j2_windows.xml -jar target\IndexerAndSearchTool-0.0.2-SNAPSHOT-jar-with-dependencies.jar --outputcharset UTF-8 --inputcharset cp850 --mode index --indexpath %cd%/documents/index --docspath %cd%/documents/input --outpath %cd%/documents/output  --numberofresults 50
PAUSE