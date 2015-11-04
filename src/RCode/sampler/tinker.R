# libs
library(ggplot2)
library(grid)
library(gridExtra)
library(scales)
library(Cairo)
library(dplyr)
#
setwd("/media/Stock/git/jmotif-GI.git/src/RCode/sampler")
ll = list.files(path=".", pattern="*.out.gz", recursive=T)
#
for(f in ll){
  print(paste(f))
  dat=read.table(gzfile(f),header=T,sep=",")
  dat$reduction=dat$compressedGrammarSize/dat$grammarSize
  names(dat) <- c("win","paa","a","approx","gSize","rules","prunedSize","prunedRules","covered","cvr","red")
  print(head(arrange(dat,red)[,c(1,2,3,4,5,6,7,8,11)]))
}
#
#
#
#
#
dat=read.table("/media/Stock/tmp/test.out",header=T,sep=",")
names(dat)
dat_cover=ggplot(dat, aes(x=grammarSize,y=approxDist, color=factor(isCovered))) + 
  geom_density2d() + ggtitle("ECG0606 time series coverage by grammar") + theme_bw() +
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
dat_cover




dat$reduction=dat$compressedGrammarSize/dat$grammarSize

datp=ggplot(filter(dat, coverage>0.99),
            aes(x=compressedGrammarSize,y=approxDist,color=factor(paa))) + 
  geom_point(alpha=0.5,size=5) + guides(color=guide_legend(ncol=2,override.aes=list(size=5,alpha=1)))+
  theme_bw()
datp

datp=ggplot(filter(dat, coverage>0.99),
            aes(x=compressedGrammarSize,y=approxDist,color=factor(paa))) + 
  geom_point(alpha=0.5,size=5) + guides(color=guide_legend(ncol=2,override.aes=list(size=5,alpha=1)))+
  theme_bw()
datp

data=ggplot(filter(dat, coverage>0.99),
            aes(x=compressedGrammarSize,y=approxDist,color=factor(alphabet))) + 
  geom_point(alpha=0.5,size=5) + guides(color=guide_legend(ncol=2,override.aes=list(size=5,alpha=1)))+
  theme_bw()
data

datr=ggplot(filter(dat, coverage>0.99),
            aes(x=compressedGrammarSize,y=approxDist,color=reduction, group=1)) + 
  geom_point(alpha=0.5,size=5) + 
  scale_colour_gradient2(low="red", high="blue", midpoint = 0.67,limits=c(0.65, 0.8)) +
  theme_bw() + 
  geom_density2d(data=dat, aes(color=factor(isCovered), group=3))
datr

dat_cover=ggplot(dat, aes(x=compressedGrammarSize,y=approxDist, color=factor(isCovered))) + 
  geom_density2d() + ggtitle("TEK16 time series coverage by grammar") + theme_bw() +
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
dat_cover






datc=ggplot(dat, aes(x=compressedGrammarSize,y=approxDist)) + 
  stat_density2d(aes(fill = factor(isCovered)), alpha=0.3, geom=c("polygon","density2d")) + 
  ggtitle("TEK16 time series coverage by grammar") + theme_bw() +
  #scale_y_continuous(limits=c(50,250)) + scale_x_continuous(limits=c(0,20000)) + 
  scale_color_discrete(guide=guide_legend(title = NULL),
                       labels=c("not covered", "covered")) +
  theme(legend.position="bottom")
datc

dat$reduction=dat$compressedGrammarSize/dat$grammarSize
head(arrange(dat[dat$coverage>0.99,],reduction))

hist(dat$reduction)
p = ggplot(dat[dat$coverage>0.99,],aes(x=reduction,y=alphabet)) + geom_point()
p




filter(ecg0606, coverage>0.8, paa>1, paa<20, approxDist>90, approxDist<100, compressedGrammarSize<15000)



tek16=read.table(gzfile("data/TEK16.updated.txt.out.gz"),header=T,sep=",")
tek16p=ggplot(filter(tek16, isCovered==1),
               aes(x=compressedGrammarSize,y=approxDist,color=factor(paa))) + 
  geom_point(alpha=0.5,size=5) + guides(color=guide_legend(ncol=2,override.aes=list(size=5,alpha=1)))+
  theme_bw()
tek16p

#
#
#
#
dat = read.table("/media/Stock/tmp/test.out", as.is=T, sep=",",header=T)
library(plyr)
library(dplyr)
head(arrange(dat, prunedRules/grammarRules))
