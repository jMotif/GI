require(reshape)
require(plyr)
require(dplyr)
#
require(stringr)
#
require(ggplot2)
require(gridExtra)
require(scales)
require(Cairo)
#
data=read.table("../../grammarsampler_updated.txt",as.is=T,header=T)
str(data)
data=data[complete.cases(data),]
#
# add the pruner efficiency measure
data$reduction=data$pruned_rules/data$rules
#
# we shall use only the data that 
ddply(data,c("dataset"), function(x){ 
  mr=min(x$reduction)
  w=x[x$reduction==mr,]$window
  p=x[x$reduction==mr,]$paa
  a=x[x$reduction==mr,]$alphabet
  data.frame(window=w,paa=p,alphabet=a,min_red=mr)
 })
#
dd=data[data$cover>0.95,]

ddply(dd,.(dataset),summarize,
      cr=cor(approximation,reduction),
      ct=cor.test(approximation,reduction)$p.value)

library(lattice)
df=data[data$dataset=="ann_gun_CentroidA1.txt",]
wireframe(approximation ~ paa * alphabet, data = df,
          xlab = "PAA", ylab = "Alphabet",
          zlab = "Approx. error",
          main = "Approximation error vs PAA and Alphabet size, Video dataset",
          drape = TRUE,
          colorkey = TRUE,
          screen = list(z = -120, x = -70)
)

# alphabet shall move the point value so Alphabet size matters 

df=dd[dd$dataset=="TEK14.txt",]
qqplot(df$approximation,df$paa*df$alphabet)
plot(df$approximation,df$alphabet)
cor(df$approximation,df$reduction)
plot(df$approximation,df$reduction)
ct=cor.test(df$approximation,df$reduction)
  
unique(dd$dataset)



df=dd[dd$dataset=="ann_gun_CentroidA1.txt",]
qqplot(df$approximation,df$reduction)
cor(df$approximation,df$reduction)
plot(df$approximation,df$reduction)

df=dd[dd$dataset=="TEK16.txt",]
df=df[complete.cases(df),]
qqplot(df$approximation,df$reduction)
cor(df$approximation,df$reduction)
plot(df$approximation,df$reduction)
head(arrange(df,reduction))
cor.test(df$approximation,df$reduction, method = "kendall", alternative = "greater")
