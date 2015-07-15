library(shiny)

# Define UI for application that plots random distributions 
shinyUI(pageWithSidebar(
  
  # Application title
  headerPanel("GrammarViz2 sampling explorer."),
  
  # Sidebar with a slider input for number of observations
  sidebarPanel(
    sliderInput("aSizeRange", 
                "SAX alphabet size range:", 
                min = 2,
                max = 12,
                step=1,
                value = c(4,6)),
  
  
  sliderInput("winSizeRange", 
              #label = h3("Sliding window size range"),
              "Sliding window size range",
              min = 30, 
              max = 590, 
              step=10,
              value = c(150, 200)),
  
  sliderInput("paaSizeRange", 
              #label = h3("Sliding window size range"),
              "PAA range",
              min = 2, 
              max = 48, 
              step=2,
              value = c(4, 8))
  ),

  # Show a plot of the generated distribution
  mainPanel(
    plotOutput("samplesPlot",height = 700)
  )
))