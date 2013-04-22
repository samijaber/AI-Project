#!/bin/bash
#put in your project folder (folder containing the folders boardgame and odd)

javac boardgame/*.java odd/*.java

for i in {1..10}
do 
	java boardgame.Server -ng &
	sleep 3
	java boardgame.Client odd.MyPlayer &
	sleep 3
	java boardgame.Client odd.MyPlayer2 &
	sleep 320

	java boardgame.Server -ng &
	sleep 3
	java boardgame.Client odd.MyPlayer2 &
	sleep 3
	java boardgame.Client odd.MyPlayer &
	sleep 305
done
