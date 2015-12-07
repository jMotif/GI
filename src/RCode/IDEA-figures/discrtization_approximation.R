require(reshape)
require(plyr)
require(dplyr)
require(data.table)
#
require(stringr)
#
require(lattice)
require(ggplot2)
require(gridExtra)
require(scales)
require(Cairo)
# The palette with grey:
cbPalette <- c("#999999", "#E69F00", "#56B4E9", "#009E73", "#F0E442", "#0072B2", 
               "#D55E00", "#CC79A7")
# The palette with black:
cbbPalette <- c("#000000", "#E69F00", "#56B4E9", "#009E73", "#F0E442", "#0072B2", 
                "#D55E00", "#CC79A7")
# To use for fills, add
# scale_fill_manual(values=cbPalette)
# To use for line and point colors, add
# scale_colour_manual(values=cbPalette)
#
data_s = fread(input = "zcat ../resources/IDEA-sampling/sampler_sequitur.out.gz")
data_s[data_s$frequency == -2147483648,]$frequency = 0
data_s = data_s[complete.cases(data_s),]
str(data_s)
df_sequitur = data.frame(select(data_s,dataset,window,paa,alphabet,rules,frequency,cover), 
                    algorithm = rep("sequitur",length(data_s$dataset)))

data_r = fread(input = "zcat ../resources/IDEA-sampling/sampler_repair.out.gz")
data_r[data_r$frequency == -2147483648,]$frequency = 0
data_r = data_r[complete.cases(data_r),]
df_repair = data.frame(select(data_r,dataset,window,paa,alphabet,rules,frequency,cover), 
                  algorithm = rep("repair",length(data_r$dataset)))
#
#
#
df_common = inner_join(df_repair, df_sequitur, by = c("dataset","window","paa","alphabet"))
unique(df_common$dataset)
#
df = select(filter(df_common, dataset == "ecg0606"), algorithm.x, frequency.x, cover.x)
setnames(df, c("algorithm.y","frequency.y","cover.y"))
df = rbind(df, select(df_common,algorithm.y,frequency.y))
setnames(df, c("algorithm","frequency"))
ggplot(df[df$frequency<50,], aes(x = frequency, fill=algorithm)) + geom_density(alpha=0.5)


df = select(filter(df_common, dataset=="stdb_308"), algorithm.x, frequency.x)
setnames(df, c("algorithm.y","frequency.y"))
df = rbind(df, select(df_common,algorithm.y,frequency.y))
setnames(df, c("algorithm","frequency"))
ggplot(df[df$frequency<50,], aes(x = frequency, fill=algorithm)) + geom_density(alpha=0.5)


df = select(filter(df_common, dataset=="ann_gun_CentroidA1"), algorithm.x, frequency.x)
setnames(df, c("algorithm.y","frequency.y"))
df = rbind(df, select(df_common,algorithm.y,frequency.y))
setnames(df, c("algorithm","frequency"))
ggplot(df[df$frequency<100,], aes(x = frequency, fill=algorithm)) + geom_density(alpha=0.5)

df = select(filter(df_common, dataset=="ann_gun_CentroidA1"), algorithm.x, frequency.x)
setnames(df, c("algorithm.y","frequency.y"))
df = rbind(df, select(df_common,algorithm.y,frequency.y))
setnames(df, c("algorithm","frequency"))
ggplot(df[df$frequency<100,], aes(x = frequency, fill=algorithm)) + geom_density(alpha=0.5)


data[data$frequency == -2147483648,]$frequency = NA
data = data[complete.cases(data),]
data$reduction = data$pruned_rules / data$rules
data = filter(data, cover > 0.95, approximation < 50)
hist(data$approximation)
#
head(arrange(data, reduction))
datasets = unique(data$dataset)
#data=filter(data,paa>3,paa<=15,alphabet>=3,alphabet<=10)
summary = ddply(data, .(dataset), function(x){
  mv = min(x$reduction)
  x[x$reduction==mv,]
})
write.table(summary, "tmp.csv",col.names=T,quote=T,row.names=F,sep=",")
#
#
#
#
#
#
dfp = data[!(data$dataset %in% c("gps_track")),]
dfp = rbind(dfp, data[(data$dataset %in% c("gps_track")) & data$window<700 & data$window>60,])
pl = dlply(data, .(dataset), function(x){
  show=FALSE
  #if(unique(x$dataset)=="gps_track" | unique(x$dataset)=="nprs43"){
  #  show=TRUE
  #}
 wireframe(approximation ~ paa * alphabet, data = x,
          xlab = "PAA", 
          ylab = "Alphabet",
          zlab = list(label="Approx. error",rot=90),
          main = paste(unique(x$dataset)),
          #par.settings = list(#box.3d = list(col = "transparent", alpha = 0),
          #axis.line = list(col = "transparent"),
          par.settings = list(clip=list(panel=FALSE)),
          drape = TRUE,
          colorkey = show,
          col.regions = rainbow(100, s = 1, v = 1, start = 0, end = max(1,100 - 1)/100, alpha = 1),
          screen = list(z = -120, x = -70)
)})
Cairo(width = 900, height = 700, 
      file="approximation_parameters.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
grid.arrange(pl[[1]], pl[[5]], pl[[7]],
             pl[[3]], pl[[8]], pl[[10]], 
             ncol=3, nrow=2)
dev.off()
#
#
#
#
#
#
# correlation between approximation and the rules total
pl = dlply(data, .(dataset), function(x){
  cor <- round(cor(x$approximation, x$rules), 4)
  pv <- round(cor.test(x$approximation, x$rules)$"p.value", 4)
  ll <- paste("r=", cor, "\npval=", pv, sep = "")
  mr <- min(x$reduction)
  dd <- data.frame(x=x[x$approximation==mr,]$rules[1],y=mr)
  #df = x[!duplicated(x[,c(5,6)]), ]
  p <- ggplot(x, aes(approximation, rules)) + geom_smooth(method = "lm") + 
    geom_jitter(alpha = 0.4, col = cbPalette[4], size = 0.35) + 
    theme_bw() + ggtitle(paste(unique(x$dataset))) +
    scale_x_continuous("Approximation error", limits = c(0,max(x$approximation))) + 
    scale_y_continuous("Number of rules in a grammar") +
    geom_text(data = NULL, label = ll, x = 0.1, y = 0.34, col = cbPalette[6], 
              hjust = 0, vjust = 0)
})  
#do.call(grid.arrange,  pl)

Cairo(width = 900, height = 450, 
      file="approximation_size_correlation.png", type="png", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 70)
grid.arrange(pl[[1]], pl[[5]], pl[[7]],
             pl[[3]], pl[[8]], pl[[10]], 
             ncol=3, nrow=2)
dev.off()


# correlation between frequency and the reduction
pl = dlply(data, .(dataset), function(x){
  cor <- round(cor(x$pruned_frequency, x$reduction), 4)
  pv <- round(cor.test(x$pruned_frequency, x$reduction)$"p.value", 4)
  ll <- paste("r=", cor, "\npval=", pv, sep = "")
  mr <- min(x$reduction)
  dd <- data.frame(x=x[x$reduction==mr,]$pruned_frequency[1],y=mr)
  #df = x[!duplicated(x[,c(13,16)]), ]
  p <- ggplot(x, aes(pruned_frequency, reduction)) + geom_smooth(method = "lm") + 
    geom_jitter(alpha = 0.4, col = cbPalette[4], size = 0.35) + 
    theme_bw() + ggtitle(paste(unique(x$dataset))) +
    scale_x_continuous("most frequent rule occurrence",limits = c(0,max(x$pruned_frequency))) + 
    scale_y_continuous(limits = c(0,1), breaks = c(0, 0.25, 0.5, 0.75, 1.0)) + 
    geom_text(data = NULL, label = ll, x = 0.25, y = 0.64, col = cbPalette[6], 
              hjust = 0, vjust = 0)
  p + geom_point(data=dd, aes(x=x, y=y), color="red", size=3.5)
})  
#do.call(grid.arrange,  pl)

Cairo(width = 900, height = 450, 
      file="reduction_frequency_correlation.png", type="png", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
grid.arrange(pl[[1]], pl[[5]], pl[[7]],
             pl[[3]], pl[[8]], pl[[10]], 
             ncol=3, nrow=2)
dev.off()

# correlation between frequency and the reduction
pl = dlply(data, .(dataset), function(x){
  cor <- round(cor(x$pruned_frequency, x$reduction), 4)
  pv <- round(cor.test(x$pruned_frequency, x$reduction)$"p.value", 4)
  ll <- paste("r=", cor, "\npval=", pv, sep = "")
  mr <- min(x$reduction)
  dd <- data.frame(x=x[x$reduction==mr,]$pruned_frequency[1],y=mr)
  p <- ggplot(x, aes(pruned_frequency, reduction)) + geom_smooth(method = "lm") + 
    geom_jitter(alpha = 0.4, col = cbPalette[4], size = 0.35) + 
    theme_bw() + ggtitle(paste(unique(x$dataset))) +
    scale_x_continuous("", limits = c(0,max(x$pruned_frequency))) + 
    scale_y_continuous(limits = c(0,1), breaks = c(0, 0.25, 0.5, 0.75, 1.0)) + 
    geom_text(data = NULL, label = ll, x = 0.25, y = 0.64, col = cbPalette[6], 
              hjust = 0, vjust = 0)
  p + geom_point(data=dd, aes(x=x, y=y), color="red", size=3.5)
})  
#do.call(grid.arrange,  pl)

Cairo(width = 900, height = 500, 
      file="reduction_frequency_correlation.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
grid.arrange(pl[[1]], pl[[5]], pl[[7]],
             pl[[3]], pl[[8]], pl[[10]], 
             ncol=3, nrow=2)
dev.off()

data = data[data$approximation<2,]
# correlation between approximation and the rules total
pl = dlply(data, .(dataset), function(x){
  if(!is.na(str_match(paste(unique(x$dataset)), "TEK"))){
    x = x[x$pruned_frequency<30,]
  }
  cor <- round(cor(x$approximation, x$rules), 4)
  pv <- round(cor.test(x$approximation, x$rules)$"p.value", 4)
  ll <- paste("r=", cor, "\npval=", pv, sep = "")
  mr <- min(x$reduction)
  dd <- data.frame(x=x[x$approximation==mr,]$rules[1],y=mr)
  p <- ggplot(x, aes(approximation, rules)) + geom_smooth(method = "lm") + 
    geom_jitter(alpha = 0.4, col = cbPalette[4], size = 0.35) + 
    theme_bw() + ggtitle(paste(unique(x$dataset))) +
    scale_x_continuous(limits = c(0,max(x$approximation))) + 
    geom_text(data = NULL, label = ll, x = 0.25, y = 0.64, col = cbPalette[6], 
              hjust = 0, vjust = 0)
  p + geom_point(data=dd, aes(x=x, y=y), color="red", size=3.5)
})  
#do.call(grid.arrange,  pl)

Cairo(width = 900, height = 500, 
      file="approximation_frequency_correlation.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
grid.arrange(pl[[1]], pl[[5]], pl[[7]],
             pl[[3]], pl[[8]], pl[[10]], 
             ncol=3, nrow=2)
dev.off()

dd=data[data$dataset=="gps_track",]
plot(dd$rules,dd$approximation)


#
# correlation between approximation and the reduction
pl = dlply(data, .(dataset), function(x){
  cor <- round(cor(x$approximation, x$reduction), 4)
  pv <- round(cor.test(x$approximation, x$reduction)$"p.value", 4)
  ll <- paste("r=", cor, "\npval=", pv, sep = "")
  print(ll)
  mr <- min(x$reduction)
  dd <- data.frame(xv=x[x$reduction==mr,]$approximation[1],yv=mr)
  print(paste(dd))
  p <- ggplot(x, aes(approximation, reduction)) + geom_smooth(method = "lm") + 
    geom_jitter(alpha = 0.4, col = cbPalette[4], size = 0.35) + 
    theme_bw() + ggtitle(paste(unique(x$dataset))) +
    scale_x_continuous(limits = c(0.2,1.25), breaks = c(0.25, 0.5, 0.75, 1.0, 1.25)) + 
    scale_y_continuous(limits = c(0,1), breaks = c(0, 0.25, 0.5, 0.75, 1.0)) + 
    geom_text(data = NULL, label = ll, x = 0.25, y = 0.64, col = cbPalette[6], 
              hjust = 0, vjust = 0)
  p + geom_point(data=dd, aes(x=xv, y=yv), color="red", size=3.5)
})  
#do.call(grid.arrange,  pl)

Cairo(width = 900, height = 500, 
      file="approximation_reduction_correlation.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
grid.arrange(pl[[1]], pl[[5]], pl[[7]],
             pl[[3]], pl[[8]], pl[[10]], 
             ncol=3, nrow=2)
dev.off()

