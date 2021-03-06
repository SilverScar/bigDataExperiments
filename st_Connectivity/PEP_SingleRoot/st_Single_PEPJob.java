/*
 * Don K Dennis (metastableB)
 * 06 July 2015
 * donkdennis [at] gmail.com
 *
 * I/O: source<tab>distance,start_point|color|adjacency list (csv)
 *
 * (c) IIIT Delhi, 2015
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataOutputStream;

public class st_Single_PEPJob extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        int iterationCount = 0; 
        String s,t;
        s = args[3];
        t = args[4];
        long connected = 0,pathLength;
        long noOfGrayNodes = 2 , runningTime, startTime, endTime, totalRunningTime = 0;

        while( noOfGrayNodes > 0 && connected == 0){
            Configuration conf = new Configuration();
            conf.set("s", s);
            conf.set("t",t);
            if(iterationCount == 0)
                conf.set("iterationCount","0");
            else
                conf.set("iterationCount","1s");
            Job st_Single_PEPJob = new Job(conf);
            String input, output;
            String JobName = new String(args[2]+ "_st_Single_PEP_"+ String.valueOf(iterationCount));
            st_Single_PEPJob.setJarByClass(getClass());
            st_Single_PEPJob.setJobName(JobName);
            if(iterationCount == 0){
               input = args[0];
               output = args[1]+"/run" + String.valueOf(iterationCount);
            } else {
                input = args[1]+"/run" + String.valueOf(iterationCount-1);
                output = args[1]+"/run" + String.valueOf(iterationCount);
            }
            FileInputFormat.setInputPaths(st_Single_PEPJob, new Path(input));
            FileOutputFormat.setOutputPath(st_Single_PEPJob, new Path(output));
            
            st_Single_PEPJob.setMapperClass(st_Single_PEPMapper.class);
            st_Single_PEPJob.setReducerClass(st_Single_PEPReducer.class);
            st_Single_PEPJob.setNumReduceTasks(1);

            st_Single_PEPJob.setOutputKeyClass(Text.class);
            st_Single_PEPJob.setOutputValueClass(Text.class);
         
            startTime = System.nanoTime();
            st_Single_PEPJob.waitForCompletion(true); 
            endTime = System.nanoTime();
            runningTime = endTime - startTime;
            totalRunningTime += runningTime;

            noOfGrayNodes = st_Single_PEPJob.getCounters().findCounter(MoreIterations.numberOfIterations).getValue();
            iterationCount++;

            System.out.println("=====================================================================");
            System.out.println ("Job Running Time = " + runningTime);
            noOfGrayNodes =  st_Single_PEPJob.getCounters().findCounter(MoreIterations.numberOfIterations).getValue();
            connected =  st_Single_PEPJob.getCounters().findCounter(MoreIterations.bothBranchesMeet).getValue(); 
            System.out.println("=====================================================================");
        }

        pathLength = iterationCount;
        if(connected == 0)
            pathLength = -1;
        System.out.println("=====================================================================");
        System.out.println("Total Running Time "+ getRunningTime(totalRunningTime));
        System.out.println("Path Length "+ pathLength);
        System.out.println("=====================================================================");
        return 0;
    }

    public static void main(String[] args) throws Exception {

        if(args.length != 5){
            System.err.println("Usage: <in> <output name>  <jobName> <s> <t>");
            System.exit(1);
        }
        int res = ToolRunner.run(new Configuration(), new st_Single_PEPJob(), args);
        System.exit(res);
    }

    public static String getRunningTime( long nanoTime) {
        long x = nanoTime / 1000000000;
        long seconds = x % 60;
        x /= 60;
        long minutes = x % 60 ;
        x /= 60;
        long hours = x % 24;
        return Long.toString(hours) + " hours," + Long.toString(minutes) +" Minutes," + Long.toString(seconds) + " seconds" ;
    }
}