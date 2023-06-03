#!/bin/bash --login
#$ -l h_rt=48000
#$ -N test
#$ -o ./logfile_$JOB_NAME.log
#$ -j y
#$ -m abe
#$ -M bogautdinov@campus.tu-berlin.de
#$ -cwd
#$ -pe mp 12
#$ -l mem_free=12G
date
hostname
runId=$JOB_NAME
#used memory Java
java_memory="-Xmx130G"
classpath="matsim-freight-BA-e846526.jar"
echo ""
echo "classpath: $classpath"
echo ""
java command
java_command="java -Djava.awt.headless=true $java_memory -cp $classpath"
main
main="org/matsim/project/run/RunMatsimFreightBerlinBA"
arguments
arguments="TEST"
command
command="$java_command $main $arguments"
echo ""
echo "command is $command"
echo ""
echo "using alternative java"
module add java/17
java -version
$command