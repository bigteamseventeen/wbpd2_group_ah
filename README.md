# YR3_WEB_PLATFORM_DEVELOPMENT_2

Web Platform Development 2 (M3I322955-18-B)

# How to run
Goto the releases section in github [here](https://github.com/bigteamseventeen/wbpd2_group_ah/releases/tag/1.0)

# How to compile 

To compile from scratch run the following commands (in order):
```
git clone https://github.com/bigteamseventeen/wbpd2_group_ah.git
cd wbpd2_group_ah

REM install the framework
cd WFramework
mvn install

REM Compile the app
cd ..
cd App
mvn package
mkdir bin

copy target/bts_g1_milestones-0.2-SNAPSHOT.jar bin
cd bin
del bts_g1_milestones-0.2-SNAPSHOT-shaded.jar
del original-bts_g1_milestones-0.2-SNAPSHOT.jar
ren bts_g1_milestones-0.2-SNAPSHOT.jar web2_group_ah.jar
```

The bin folder will now contain the web2_group_ah.jar, to run this just execute `java -jar web2_group_ah.jar`
