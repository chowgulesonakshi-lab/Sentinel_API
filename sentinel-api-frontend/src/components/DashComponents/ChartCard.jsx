import {
    ResponsiveContainer,
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Label
} from "recharts";


function CustomTooltip({ active, payload, label }) {

    if (active && payload && payload.length) {

        return (
            <div className="
                bg-transparent
                p-2
            ">
                <p className="
                    text-blue-600
                    font-medium
                    text-sm
                ">
                    {label}
                </p>

                <p className="
                    text-blue-600
                    font-semibold
                    text-sm
                ">
                    Response Time: {payload[0].value} ms
                </p>

            </div>
        );
    }

    return null;
}


function ChartCard({ metrics }) {

    const chartData = metrics.map((metric) => ({
        time: new Date(metric.timeStamp).toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
        }),
        responseTime: metric.responseTime,
    }));


    return (
        <div className="
            bg-white dark:bg-slate-900
            rounded-2xl
            shadow-sm
            hover:shadow-md
            p-6
            border border-slate-200 dark:border-slate-800
            transition-all duration-300
        ">


            <h2 className="
                text-lg
                font-semibold
                text-slate-800 dark:text-white
            ">
                Performance Overview
            </h2>


            <p className="
                text-sm
                text-slate-500 dark:text-slate-400
                mt-1
                mb-6
            ">
                API response time over recent health checks
            </p>



            {chartData.length === 0 ? (

                <div className="
                    h-72
                    flex
                    items-center
                    justify-center
                    text-slate-400 dark:text-slate-500
                ">
                    No metrics available
                </div>

            ) : (

                <ResponsiveContainer width="100%" height={320}>

                    <LineChart data={chartData}>

                        <CartesianGrid
                            strokeDasharray="3 3"
                            stroke="currentColor"
                            opacity={0.15}
                        />


                        <XAxis
                            dataKey="time"
                            tick={{
                                fill: "var(--chart-text)",
                                fontSize: 12
                            }}
                            stroke="var(--chart-text)"
                        >
                            <Label
                                value="Time"
                                offset={-5}
                                position="insideBottom"
                                fill="var(--chart-text)"
                            />
                        </XAxis>


                        <YAxis
                            tick={{
                                fill: "var(--chart-text)",
                                fontSize: 12
                            }}
                            stroke="var(--chart-text)"
                        >
                            <Label
                                value="Response Time (ms)"
                                angle={-90}
                                position="insideLeft"
                                fill="var(--chart-text)"
                                style={{
                                    textAnchor: "middle"
                                }}
                            />
                        </YAxis>



                        <Tooltip
                            content={<CustomTooltip />}
                        />


                        <Line
                            type="monotone"
                            dataKey="responseTime"
                            stroke="#2563eb"
                            strokeWidth={3}
                            dot={{
                                r: 4,
                                fill: "#2563eb"
                            }}
                            activeDot={{
                                r: 6
                            }}
                        />


                    </LineChart>

                </ResponsiveContainer>

            )}

        </div>
    );
}


export default ChartCard;