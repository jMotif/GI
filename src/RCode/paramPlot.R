library(ggplot2)
library(dplyr)
dat=read.table("rules_num.txt",skip=1,header=F,sep=",")
names(dat) <- c("window","paa","alphabet","isCovered","grammarsize",
                "compressedSize","approxDist")

dd <- filter(dat, isCovered==1)

p=ggplot(dat, aes(x=compressedSize,y=approxDist,
                  color=factor(isCovered))) + 
  geom_density2d() + ggtitle("Coverage of ECG0606 by rule intervals")
p         

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
