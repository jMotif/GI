library(ggplot2)
library(dplyr)
data=read.csv("../../rules_num.txt")
names(data) <- c("window","paa","alphabet","size","approx_dist")
range(data$alphabet)
range(data$approx_dist)
range(data$size)

filter(data, window==170, paa==4, alphabet==4)

filter(data, window==170, paa==6, alphabet==3)

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
