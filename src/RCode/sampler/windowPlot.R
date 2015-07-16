# libs
library(ggplot2)
library(grid)
library(gridExtra)
library(Cairo)
library(dplyr)
# data
setwd("/home/psenin/git/jmotif-gi/src/RCode/sampler")
#
tek14=read.table(gzfile("data/ann_gun_CentroidA1.csv.out.gz"),header=T,sep=",")

p=ggplot(filter(tek14, isCovered==1),
         aes(x=approxDist,y=factor(window))) + 
  geom_point(alpha=0.5)
p

p=ggplot(filter(tek14, isCovered==1),
         aes(y=approxDist,x=alphabet)) + 
  geom_point(alpha=0.5)
p

tek14_cover=ggplot(tek14, aes(x=compressedGrammarSize,y=approxDist, color=factor(isCovered))) + 
  geom_density2d() + ggtitle("TEK14 time series coverage by grammar") + theme_bw() +
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
tek14_cover

ecg0606=read.table(gzfile("data/ecg0606_1.csv.out.gz"),header=T,sep=",")
ecg0606_cover=ggplot(ecg0606, aes(x=compressedGrammarSize,y=approxDist, color=factor(isCovered))) + 
  geom_density2d() + ggtitle("ECG0606 time series coverage by grammar") + theme_bw() +
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
ecg0606_cover

video=read.table(gzfile("data/ann_gun_CentroidA1.csv.out.gz"),header=T,sep=",")
video_cover=ggplot(video, aes(x=compressedGrammarSize,y=approxDist, color=factor(isCovered))) + 
  geom_density2d() + ggtitle("Video dataset time series coverage by grammar") + theme_bw() +
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
video_cover

respiration=read.table(gzfile("data/nprs43.txt.out.gz"),header=T,sep=",")
respiration_cover=ggplot(video, aes(x=compressedGrammarSize,y=approxDist, color=factor(isCovered))) + 
  geom_density2d() + ggtitle("Respiration dataset time series coverage by grammar") + theme_bw() +
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
respiration_cover

#p = rectGrob()
#grid.arrange(p, arrangeGrob(p,p,p, heights=c(3/4, 1/4, 1/4), ncol=1), ncol=2)
grid.arrange(ecg0606_cover, video_cover, tek14_cover, respiration_cover, ncol=2)

CairoPDF(file = "cover_4",
         width = 12, height = 8, onefile = TRUE, family = "Helvetica",
         title = "R Graphics Output", fonts = NULL, version = "1.1",
         paper = "special")
print(grid.arrange(ecg0606_cover, video_cover, tek14_cover, respiration_cover, ncol=2))
dev.off()
