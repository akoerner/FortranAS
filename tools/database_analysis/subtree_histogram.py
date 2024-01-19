import sqlite3
import matplotlib.pyplot as plt
import numpy as np
from scipy.stats import gaussian_kde, percentileofscore

conn = sqlite3.connect('FortranAS.sqlite3')
cursor = conn.cursor()

query_depth = "SELECT depth FROM subtrees WHERE size <= 100;"
query_size = "SELECT size FROM subtrees WHERE size <= 100;"

cursor.execute(query_depth)
depths = np.array(cursor.fetchall()).flatten()

cursor.execute(query_size)
sizes = np.array(cursor.fetchall()).flatten()

conn.close()

fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(10, 8))

ax1.hist(depths, bins=np.arange(min(depths), max(depths) + 2) - 0.5, rwidth=0.8, density=False, align='mid', color='blue', edgecolor='black')
ax1.set_ylabel('Frequency')
ax1.set_title('Subtree Depth')

ax1.set_xlabel('Subtree Depth')

kde_depth = gaussian_kde(depths)
x_vals_depth = np.arange(min(depths), max(depths) + 1, 1)
ax1.plot(x_vals_depth, kde_depth(x_vals_depth) * len(depths), color='red', label='Density Curve')

percentiles=[25, 50, 75]
quantiles_depth = np.percentile(depths, percentiles)
bins_depth = np.arange(min(depths), max(depths) + 1, 1)
percentile_index = 0 
for quantile in quantiles_depth:
    bin_index = int(np.digitize(quantile, bins_depth))
    ax1.axvline(quantile, linestyle='dotted', color='black', linewidth=1)
    ax1.text(quantile, ax1.get_ylim()[1] * 1.01, f'Q{percentiles[percentile_index]}%\nbin:{bin_index}', ha='center')
    percentile_index = percentile_index + 1

ax2.hist(sizes, bins=np.arange(min(sizes), max(sizes) + 2) - 0.5, rwidth=0.8, density=False, align='mid', color='orange', edgecolor='black')
ax2.set_xlabel('Subtree Size')
ax2.set_ylabel('Frequency')
ax2.set_title('Subtree Size')

kde_size = gaussian_kde(sizes)
x_vals_size = np.arange(min(sizes), max(sizes) + 1, 1)
ax2.plot(x_vals_size, kde_size(x_vals_size) * len(sizes), color='red', label='Density Curve')

quantiles_size = np.percentile(sizes, percentiles)
bins_size = np.arange(min(sizes), max(sizes) + 1, 1)
percentile_index = 0 
for quantile in quantiles_size:
    bin_index = int(np.digitize(quantile, bins_size))
    ax2.axvline(quantile, linestyle='dotted', color='black', linewidth=1)
    ax2.text(quantile, ax2.get_ylim()[1] * 1.01, f'Q{percentiles[percentile_index]}%\nbin: {bin_index}', ha='center')
    percentile_index = percentile_index + 1
    print(percentile_index)

fig.suptitle('FortranAS.sqlite3 Subtree Histograms', fontsize=16)

ax1.legend(['Density'] + ['Frequency'])
ax2.legend(['Density'] + ['Frequency'])

plt.tight_layout()

plt.savefig('histogram.png')
plt.show()

