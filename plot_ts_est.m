data = importdata('period_est.txt');
loop1 = data(1:4:end,:);
loop2 = data(2:4:end,:);
loop3 = data(3:4:end,:);
loop4 = data(4:4:end,:);
nrow = size(loop1,1);
for i = 1:4
    count = 1;
    sampsize = size(loop1,1);
    figure;
    hold on;
    for j = 1:sampsize
        plotdatay = linspace(loop1(j,1),loop1(j,2),10);
        plotdatax = j*ones(1,10);
        plot(plotdatax,plotdatay);
    end
    hold off;
end