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
range(df_common$window)
range(df_common$paa)
range(df_common$alphabet)

#
df = select(filter(df_common, dataset == "ecg0606"), algorithm.x, frequency.x, cover.x)
setnames(df, c("algorithm.y","frequency.y","cover.y"))
df = rbind(df, select(df_common,algorithm.y,frequency.y,cover.y))
setnames(df, c("algorithm","frequency","cover"))
ecg0606_density <- ggplot(df[df$cover>0.98 & df$frequency < 100,], aes(x = frequency, fill=algorithm)) + 
  geom_density(alpha=0.5, binwidth=1) + theme_bw() +
  ggtitle(paste("Estimated kernel densities for the most frequent rule occurrence\n",
    "in ECG0606 when the total cover is above 0.98")) +
  scale_x_continuous(limits=c(0,50),breaks=seq(0,50,by=10)) +
  theme(legend.position="bottom",legend.direction="horizontal")
#
dd = read.table("../resources/test-data/ecg0606.txt")
df_ecg = data.frame(x=c(1:length(dd$V1)), y=dd$V1)
ecg0606_plot <- ggplot(data=df_ecg, aes(x=x,y=y)) + geom_line(col=cbbPalette[6]) +
  theme_bw() + ggtitle("Dataset ECG0606 with abnormal heartbeat highlighted") + 
  geom_path(data=df_ecg[411:511,], col="red")

df = select(filter(df_common, dataset == "ann_gun_CentroidA1"), algorithm.x, frequency.x, cover.x)
setnames(df, c("algorithm.y","frequency.y","cover.y"))
df = rbind(df, select(df_common,algorithm.y,frequency.y,cover.y))
setnames(df, c("algorithm","frequency","cover"))
video_density <- ggplot(df[df$cover>0.98 & df$frequency < 150,], aes(x = frequency, fill=algorithm)) + 
  geom_density(alpha=0.5, binwidth=1) + theme_bw() +
  ggtitle(paste("Estimated kernel densities for the most frequent rule occurrence\n",
                "in Video dataset when the total cover is above 0.98")) +
  scale_x_continuous(limits=c(0,150),breaks=seq(0,150,by=25)) +
  theme(legend.position="bottom",legend.direction="horizontal")
#
dd = read.table("../resources/test-data/ann_gun_CentroidA1.txt")
df_video = data.frame(x=c(1:length(dd$V1)), y=dd$V1)
video_plot <- ggplot(data=df_video, aes(x=x,y=y)) + geom_line(col=cbbPalette[6]) +
  theme_bw() + ggtitle("Video dataset (~70 normal cycles)") +
  scale_y_continuous(limits=c(180,500)) 

Cairo(width = 1100, height = 400, 
      file="densities.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
grid.arrange(ecg0606_plot,video_plot,ecg0606_density, video_density,ncol=2,
             heights=c(2/5, 3/5))
dev.off()
