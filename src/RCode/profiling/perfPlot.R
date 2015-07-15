require(reshape)
require(Cairo)
require(scales)
require(ggplot2)
require(grid)
require(gridExtra)

data=read.csv("perf.csv",header=F,sep=" ")
str(data)

df=data.frame(threads=unlist(data$V1),ms=unlist(data$V2))
library(plyr)
df=ddply(df,.(threads),summarize,mean(ms))
names(df) <- c("threads","ms")

scientific_10 <- function(x) {
  parse(text=gsub("e", " %*% 10^", scientific_format()(x)))
}

p=ggplot(df[df$threads<8,],aes(x=threads,y=ms)) + 
  geom_line(size=2) +  ggtitle("Multi-threaded RePair GI performance") +
  scale_y_continuous(label=scientific_10)
p = p + theme(axis.text=element_text(size=14),
              axis.title=element_text(size=16,face="bold"),
              plot.title=element_text(size=20,face="bold"))
png("profiling.png", width=700, height=400, pointsize = 20)
print(p)
dev.off() 

