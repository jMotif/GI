# libs
library(ggplot2)
library(grid)
library(gridExtra)
library(Cairo)
library(dplyr)
# data
setwd("/media/Stock/git/jmotif-GI.git/src/RCode/sampler")
#
tek14=read.table(gzfile("data/TEK14.txt.gz"),header=F,sep=",")
tek14$x=seq(1:length(tek14$V1))
tek14_cover=ggplot(tek14, aes(x=x,y=V1)) + geom_line() + 
  ggtitle("TEK14 dataset") + theme_bw() + theme(axis.title.y=element_blank())
tek14_cover

ecg0606=read.table(gzfile("data/ecg0606_1.csv.gz"),header=F,sep=",")
ecg0606$x=seq(1:length(ecg0606$V1))
ecg0606_cover=ggplot(ecg0606, aes(x=x,y=V1)) + geom_line() + 
  ggtitle("ECG0606 dataset") + theme_bw() + theme(axis.title.y=element_blank())
ecg0606_cover

video=read.table(gzfile("data/ann_gun_CentroidA1.csv.gz"),header=F,sep=",")
video$x=seq(1:length(video$V1))
video_cover=ggplot(video, aes(x=x,y=V1)) + geom_line() + 
  ggtitle("Video dataset") + theme_bw() + theme(axis.title.y=element_blank())
video_cover

respiration=read.table(gzfile("data/nprs43.txt.gz"),header=F,sep=",")
respiration$x=seq(1:length(respiration$V1))
respiration_cover=ggplot(respiration, aes(x=x,y=V1)) + geom_line() + 
  ggtitle("Respiration dataset") + theme_bw() + theme(axis.title.y=element_blank())
respiration_cover

#p = rectGrob()
#grid.arrange(p, arrangeGrob(p,p,p, heights=c(3/4, 1/4, 1/4), ncol=1), ncol=2)
grid.arrange(ecg0606_cover, video_cover, tek14_cover, respiration_cover, ncol=2)

CairoPDF(file = "data_4",
         width = 12, height = 6, onefile = TRUE, family = "Helvetica",
         title = "R Graphics Output", fonts = NULL, version = "1.1",
         paper = "special")
print(grid.arrange(ecg0606_cover, video_cover, tek14_cover, respiration_cover, ncol=2))
dev.off()
