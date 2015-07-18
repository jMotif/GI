# libs
library(ggplot2)
library(grid)
library(gridExtra)
library(scales)
library(Cairo)
library(dplyr)
#
ecg0606=read.table(gzfile("data/ecg0606_1.updated.csv.out.gz"),header=T,sep=",")
names(ecg0606)
ecg0606p=ggplot(filter(ecg0606, isCovered==1),
         aes(x=compressedGrammarSize,y=approxDist,color=factor(paa))) + 
  geom_point(alpha=0.5,size=5) + guides(color=guide_legend(ncol=2,override.aes=list(size=5,alpha=1)))+
  theme_bw()
ecg0606p

ecg0606a=ggplot(filter(ecg0606, isCovered==1),
          aes(x=compressedGrammarSize,y=approxDist,color=factor(alphabet))) + 
  geom_point(alpha=0.5,size=5) + guides(color=guide_legend(ncol=2,override.aes=list(size=5,alpha=1)))+
  theme_bw()
ecg0606a

ecg0606w=ggplot(filter(ecg0606, isCovered==1, compressedGrammarSize<20000),
                aes(x=compressedGrammarSize,y=approxDist,color=factor(window))) + 
  geom_point(alpha=0.5,size=5) + guides(color=guide_legend(ncol=2,override.aes=list(size=5,alpha=1)))+
  theme_bw()
ecg0606w

