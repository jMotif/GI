# libs
library(ggplot2)
library(grid)
library(gridExtra)
library(Cairo)
library(dplyr)
# data
setwd("/media/Stock/git/jmotif-GI.git/src/RCode/sampler")
#
tek16=read.table(gzfile("data/TEK16.updated.txt.out.gz"),header=T,sep=",")
tek16$covered90=tek16$coverage>0.9
tek16_cover=ggplot(tek16, aes(x=compressedGrammarSize,y=approxDist, color=factor(covered90))) + 
  geom_density2d() + ggtitle("TEK16 time series 90% coverage by grammar") + theme_bw() +
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
tek16_cover

ecg0606=read.table(gzfile("data/ecg0606_1.updated.csv.out.gz"),header=T,sep=",")
ecg0606$covered90=ecg0606$coverage>0.9
ecg0606_cover=ggplot(ecg0606, aes(x=compressedGrammarSize,y=approxDist, color=factor(covered90))) + 
  geom_density2d() + ggtitle("ECG0606 time series 90% coverage by grammar") + theme_bw() +
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
ecg0606_cover

video=read.table(gzfile("data/ann_gun_CentroidA1.updated.csv.out.gz"),header=T,sep=",")
video$covered90=video$coverage>0.9
video_cover=ggplot(video, aes(x=compressedGrammarSize,y=approxDist, color=factor(covered90))) + 
  geom_density2d() + ggtitle("Video dataset time series 90% coverage by grammar") + theme_bw() +
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
video_cover

respiration=read.table(gzfile("data/nprs43.updated.txt.out.gz"),header=T,sep=",")
respiration$covered90=respiration$coverage>0.9
respiration_cover=ggplot(respiration, aes(x=compressedGrammarSize,y=approxDist, color=factor(covered90))) + 
  geom_density2d() + ggtitle("Respiration dataset time series 90% coverage by grammar") + theme_bw() +
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
respiration_cover

#p = rectGrob()
#grid.arrange(p, arrangeGrob(p,p,p, heights=c(3/4, 1/4, 1/4), ncol=1), ncol=2)
grid.arrange(ecg0606_cover, video_cover, tek16_cover, respiration_cover, ncol=2)

CairoPDF(file = "cover_4_90",
         width = 12, height = 8, onefile = TRUE, family = "Helvetica",
         title = "R Graphics Output", fonts = NULL, version = "1.1",
         paper = "special")
print(grid.arrange(ecg0606_cover, video_cover, tek16_cover, respiration_cover, ncol=2))
dev.off()
