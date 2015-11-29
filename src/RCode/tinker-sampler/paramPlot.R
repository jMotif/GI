# libs
library(ggplot2)
library(grid)
library(gridExtra)
library(Cairo)
library(dplyr)
# data
setwd("/media/Stock/git/jmotif-GI.git/src/RCode/sampler")
#
tek14=read.table(gzfile("data/TEK14.txt.out.gz"),header=T,sep=",")
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
         width = 12, height = 6, onefile = TRUE, family = "Helvetica",
         title = "R Graphics Output", fonts = NULL, version = "1.1",
         paper = "special")
print(grid.arrange(ecg0606_cover, video_cover, tek14_cover, respiration_cover, ncol=2))
dev.off()




dd=read.table(gzfile("data/nprs43.txt.out.gz"),header=T,sep=",")
cover_plot=ggplot(dd, aes(x=compressedGrammarSize,y=approxDist,
                               color=factor(isCovered))) + 
  geom_density2d() + ggtitle("Coverage of ECG0606 by rule intervals")
cover_plotec



dd <- filter(ecg0606, isCovered==1)

p=ggplot(dat, aes(x=compressedSize,y=approxDist,
                  color=factor(paa))) + 
  geom_point(alpha=0.5) +
  guides(color=guide_legend(ncol=4,override.aes=list(size=5,alpha=1)))
p         

         
p1 <- ggplot(dd, aes(x=window, approxDist)) + geom_point() +
  ggtitle("")
p1 + geom_point()

p2 <- ggplot(dd, aes(paa, approxDist))
p2 + geom_point()

p3 <- ggplot(dd, aes(alphabet, approxDist))
p3 + geom_point()

p4 <- ggplot(dd, aes(x=window, grammarsize, color=factor(paa))) + geom_point() +
  ggtitle("")
p4 + geom_point()

p2 <- ggplot(dd, aes(paa, approxDist))
p2 + geom_point()

p3 <- ggplot(dd, aes(alphabet, approxDist))
p3 + geom_point()




stat_density2d(aes(fill = ..level..), geom="polygon")


         filter(data, window<600, approx_dist<600, size<20000, alphabet<8),
         aes(x=size,y=approx_dist,color=factor(paa),shape=factor(alphabet),size=factor(window))) + 
  geom_point(alpha=0.5) + guides(color=guide_legend(ncol=4,override.aes=list(size=5,alpha=1))) + 
  guides(shape=guide_legend(ncol=2),override.aes=list(size=4)) + guides(size=guide_legend(ncol=3)) +
  theme_bw() + scale_shape_manual(values=seq(0,10))
p


unique(dd$window)
unique(dd$alphabet)
unique(dd$paa)



filter(data, approx_dist <10)

names(data)
p=ggplot(filter(data, window<600, approx_dist<600, size<20000, alphabet<8),
         aes(x=size,y=approx_dist,color=factor(paa),shape=factor(alphabet),size=factor(window))) + 
  geom_point(alpha=0.5) + guides(color=guide_legend(ncol=4,override.aes=list(size=5,alpha=1))) + 
  guides(shape=guide_legend(ncol=2),override.aes=list(size=4)) + guides(size=guide_legend(ncol=3)) +
  theme_bw() + scale_shape_manual(values=seq(0,10))
p

p + geom_point(data=filter(data, paa==4, alphabet==4, window==170), aes(x=size,y=approx_dist), color="orange")

library(shiny)
runExample("01_hello")

min(data$size)

filter(data, size==min(data$size))

table(filter(data,alphabet==6))

shinyapps::deployApp('/home/psenin/git/jmotif-gi/src/RCode/samplerApp')
