# libs
library(ggplot2)
library(grid)
library(gridExtra)
library(scales)
library(Cairo)
library(dplyr)
#
setwd("/media/Stock/git/jmotif-GI.git/src/RCode/sampler")
ll = list.files(path=".", pattern="out.gz", recursive=T)
#
for(f in ll){
  print(paste(f))
  dat=read.table(gzfile(f),header=T,sep=",")
  dat$reduction=dat$compressedGrammarSize/dat$grammarSize
  print(head(arrange(dat,reduction)))
}


p2=ggplot(dat[dat$isCovered==1,],aes(approxDist,compressedGrammarSize)) + 
  stat_density2d(aes(fill = isCovered), alpha=0.3, color="cyan3",
                 fill="cyan", geom=c("polygon","density2d"))
p2

+
  scale_x_continuous("Approximation distance",limits=range(c(0,200))) +
  scale_y_continuous("Grammar size", limits=range(dd$grammarSize)) + 
  ggtitle("Area with successful rule density-based\n discovery of the true anomaly")+
  theme_bw() + theme(legend.position="bottom") 
p2 




dat=read.table(gzfile("data/mitdbx_mitdbx_108_1.updated.txt.out.gz"),header=T,sep=",")
names(dat)
range(dat$paa)
datp=ggplot(filter(dat, coverage>0.99, paa>1, paa<20),
         aes(x=compressedGrammarSize,y=approxDist,color=factor(paa))) + 
  geom_point(alpha=0.5,size=5) + guides(color=guide_legend(ncol=2,override.aes=list(size=5,alpha=1)))+
  theme_bw()
datp

datc=ggplot(dat, aes(x=compressedGrammarSize,y=approxDist)) + 
  stat_density2d(aes(fill = factor(isCovered)), alpha=0.3, geom=c("polygon","density2d")) + 
  ggtitle("TEK16 time series coverage by grammar") + theme_bw() +
  #scale_y_continuous(limits=c(50,250)) + scale_x_continuous(limits=c(0,20000)) + 
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
datc

dat$reduction=dat$compressedGrammarSize/dat$grammarSize
head(arrange(dat,reduction))
hist(dat$reduction)
p = ggplot(dat[dat$coverage>0.9,],aes(x=reduction,y=alphabet)) + geom_point()
p




filter(ecg0606, coverage>0.8, paa>1, paa<20, approxDist>90, approxDist<100, compressedGrammarSize<15000)



tek16=read.table(gzfile("data/TEK16.updated.txt.out.gz"),header=T,sep=",")
tek16p=ggplot(filter(tek16, isCovered==1),
               aes(x=compressedGrammarSize,y=approxDist,color=factor(paa))) + 
  geom_point(alpha=0.5,size=5) + guides(color=guide_legend(ncol=2,override.aes=list(size=5,alpha=1)))+
  theme_bw()
tek16p
