import pandas as pd
import numpy as np
from matplotlib import pyplot as plt

with open('statistics.csv', 'r') as csv:
    content = csv.readlines()
    content = [line.strip() for line in content]
    
headers = content[0].split(', ')
data = [timeStep.split(', ') for timeStep in content[1:]]

df = pd.DataFrame(data, columns=headers)[1:]
df['Infections'] = pd.to_numeric(df['Infections'])
df['Deaths'] = pd.to_numeric(df['Deaths'])
df['Removed Cells'] = pd.to_numeric(df['Removed Cells'])

plot = df.plot(title='Epidemic Simulation')
plot.set_xlabel('Time steps (days)')
plot.set_ylabel('# of Cells')
plt.xticks(np.arange(min(df.index), max(df.index)+1, 1.0))
fig = plot.get_figure()
fig.savefig('Simulation.png')
