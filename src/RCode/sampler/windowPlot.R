# libs
library(ggplot2)
library(grid)
library(gridExtra)
library(scales)
library(Cairo)
library(dplyr)
# data
setwd("/home/psenin/git/jmotif-gi/src/RCode/sampler")
#
ecg0606=read.table(gzfile("data/ecg0606_1.csv.out.gz"),header=T,sep=",")
p11=ggplot(filter(ecg0606, isCovered==1),aes(x=approxDist,y=window)) + 
  xlab("Approximation distance") + ylab("Sliding window size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("ECG0606: approximation distance vs window size")
p11

p12=ggplot(filter(ecg0606, isCovered==1),aes(x=compressedGrammarSize,y=window)) + 
  xlab("Compressed grammar size") + ylab("Sliding window size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("ECG0606: grammar size vs window size")
p12

p13=ggplot(filter(ecg0606, isCovered==1),aes(x=approxDist,y=paa)) + 
  xlab("Approximation distance") + ylab("PAA size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("ECG0606: approximation distance vs PAA size")
p13

p14=ggplot(filter(ecg0606, isCovered==1),aes(x=compressedGrammarSize,y=paa)) + 
  xlab("Compressed grammar size") + ylab("PAA size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("ECG0606: grammar size vs PAA size")
p14

p15=ggplot(filter(ecg0606, isCovered==1),aes(x=approxDist,y=alphabet)) + 
  xlab("Approximation distance") + ylab("Alphabet size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("ECG0606: approximation distance vs Alphabet size")
p15

p16=ggplot(filter(ecg0606, isCovered==1),aes(x=compressedGrammarSize,y=alphabet)) + 
  xlab("Compressed grammar size") + ylab("Alphabet size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("ECG0606: grammar size vs Alphabet size")
p16

#p = rectGrob()
#grid.arrange(p, arrangeGrob(p,p,p, heights=c(3/4, 1/4, 1/4), ncol=1), ncol=2)
grid.arrange(p11, p12, p13, p14, p15, p16, ncol=1)


video=read.table(gzfile("data/ann_gun_CentroidA1.csv.out.gz"),header=T,sep=",")
p21=ggplot(filter(video, isCovered==1),aes(x=approxDist,y=window)) + 
  xlab("Approximation distance") + ylab("Sliding window size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("Video: approximation distance vs window size")
p21

p22=ggplot(filter(video, isCovered==1),aes(x=compressedGrammarSize,y=window)) + 
  xlab("Compressed grammar size") + ylab("Sliding window size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("Video: grammar size vs window size")
p22

p23=ggplot(filter(video, isCovered==1),aes(x=approxDist,y=paa)) + 
  xlab("Approximation distance") + ylab("PAA size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("Video: approximation distance vs PAA size")
p23

p24=ggplot(filter(video, isCovered==1),aes(x=compressedGrammarSize,y=paa)) + 
  xlab("Compressed grammar size") + ylab("PAA size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("Video: grammar size vs PAA size")
p24

p25=ggplot(filter(video, isCovered==1),aes(x=approxDist,y=alphabet)) + 
  xlab("Approximation distance") + ylab("Alphabet size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("Video: approximation distance vs Alphabet size")
p25

p26=ggplot(filter(video, isCovered==1),aes(x=compressedGrammarSize,y=alphabet)) + 
  xlab("Compressed grammar size") + ylab("Alphabet size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("Video: grammar size vs Alphabet size")
p26
#
grid.arrange(p11, p21, p12, p22, p13, p23, p14, p24, p15, p25, p16, p26, ncol=2)


CairoPDF(file = "ecg_video_corr",
         width = 12, height = 17, onefile = TRUE, family = "Helvetica",
         title = "R Graphics Output", fonts = NULL, version = "1.1",
         paper = "special")
grid.arrange(p11, p21, p12, p22, p13, p23, p14, p24, p15, p25, p16, p26, ncol=2)
dev.off()

#
tek16=read.table(gzfile("data/TEK16.txt.out.gz"),header=T,sep=",")
p11=ggplot(filter(tek16, isCovered==1),aes(x=approxDist,y=window)) + 
  xlab("Approximation distance") + ylab("Sliding window size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("TEK16: approximation distance vs window size")
p11

p12=ggplot(filter(tek16, isCovered==1),aes(x=compressedGrammarSize,y=window)) + 
  xlab("Compressed grammar size") + ylab("Sliding window size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("TEK16: grammar size vs window size")
p12

p13=ggplot(filter(tek16, isCovered==1),aes(x=approxDist,y=paa)) + 
  xlab("Approximation distance") + ylab("PAA size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("TEK16: approximation distance vs PAA size")
p13

p14=ggplot(filter(tek16, isCovered==1),aes(x=compressedGrammarSize,y=paa)) + 
  xlab("Compressed grammar size") + ylab("PAA size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("TEK16: grammar size vs PAA size")
p14

p15=ggplot(filter(tek16, isCovered==1),aes(x=approxDist,y=alphabet)) + 
  xlab("Approximation distance") + ylab("Alphabet size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("TEK16: approximation distance vs Alphabet size")
p15

p16=ggplot(filter(tek16, isCovered==1),aes(x=compressedGrammarSize,y=alphabet)) + 
  xlab("Compressed grammar size") + ylab("Alphabet size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("TEK16: grammar size vs Alphabet size")
p16

#p = rectGrob()
#grid.arrange(p, arrangeGrob(p,p,p, heights=c(3/4, 1/4, 1/4), ncol=1), ncol=2)
grid.arrange(p11, p12, p13, p14, p15, p16, ncol=1)


nprs=read.table(gzfile("data/nprs43.txt.out.gz"),header=T,sep=",")
p21=ggplot(filter(nprs, isCovered==1),aes(x=approxDist,y=window)) + 
  xlab("Approximation distance") + ylab("Sliding window size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("NPRS: approximation distance vs window size")
p21

p22=ggplot(filter(nprs, isCovered==1),aes(x=compressedGrammarSize,y=window)) + 
  xlab("Compressed grammar size") + ylab("Sliding window size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("NPRS: grammar size vs window size")
p22

p23=ggplot(filter(nprs, isCovered==1),aes(x=approxDist,y=paa)) + 
  xlab("Approximation distance") + ylab("PAA size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("NPRS: approximation distance vs PAA size")
p23

p24=ggplot(filter(nprs, isCovered==1),aes(x=compressedGrammarSize,y=paa)) + 
  xlab("Compressed grammar size") + ylab("PAA size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("NPRS: grammar size vs PAA size")
p24

p25=ggplot(filter(nprs, isCovered==1),aes(x=approxDist,y=alphabet)) + 
  xlab("Approximation distance") + ylab("Alphabet size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("NPRS: approximation distance vs Alphabet size")
p25

p26=ggplot(filter(nprs, isCovered==1),aes(x=compressedGrammarSize,y=alphabet)) + 
  xlab("Compressed grammar size") + ylab("Alphabet size") +
  geom_point(alpha=0.5,size=2) + theme_bw() + ggtitle("NPRS: grammar size vs Alphabet size")
p26
#
grid.arrange(p11, p21, p12, p22, p13, p23, p14, p24, p15, p25, p16, p26, ncol=2)


CairoPDF(file = "tek_nprs_corr",
         width = 12, height = 17, onefile = TRUE, family = "Helvetica",
         title = "R Graphics Output", fonts = NULL, version = "1.1",
         paper = "special")
grid.arrange(p11, p21, p12, p22, p13, p23, p14, p24, p15, p25, p16, p26, ncol=2)
dev.off()

