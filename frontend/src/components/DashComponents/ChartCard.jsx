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

    if (!active || !payload || !payload.length) {
        return null;
    }

    return (
        <div className="
            bg-white dark:bg-slate-800
            border border-slate-200 dark:border-slate-700
            rounded-lg
            shadow-lg
            px-4 py-3
        ">

            <p className="text-sm text-slate-500 dark:text-slate-400">
                {label}
            </p>

            <p className="mt-1 font-semibold text-blue-600">
                {payload[0].value.toFixed(0)} ms
            </p>

        </div>
    );
}

function ChartCard({ metrics }) {

    const chartData = [...metrics]
        .sort(
            (a, b) =>
                new Date(a.timeStamp) - new Date(b.timeStamp)
        )
        .slice(-20)
        .map(metric => ({
            time: new Date(metric.timeStamp).toLocaleTimeString([], {
                hour: "2-digit",
                minute: "2-digit"
            }),
            responseTime: metric.responseTime
        }));

    return (
        <div className="
            bg-white dark:bg-slate-900
            rounded-2xl
            shadow-sm hover:shadow-md
            border border-slate-200 dark:border-slate-800
            p-6
            transition-all duration-300
        ">

            <h2 className="
                text-lg font-semibold
                text-slate-900 dark:text-white
            ">
                Performance Trend
            </h2>

            <p className="
                mt-1 mb-6
                text-sm
                text-slate-500 dark:text-slate-400
            ">
                Average response time over recent health checks
            </p>

            {chartData.length === 0 ? (

                <div className="
                    h-80
                    flex items-center justify-center
                    text-slate-500 dark:text-slate-400
                ">
                    No performance data available.
                </div>

            ) : (

                <ResponsiveContainer width="100%" height={320}>

                    <LineChart data={chartData}>

                        <CartesianGrid
                            strokeDasharray="3 3"
                            stroke="currentColor"
                            opacity={0.1}
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
                                position="insideBottom"
                                offset={-5}
                                fill="var(--chart-text)"
                            />

                        </XAxis>

                        <YAxis
                            tickFormatter={(value) => `${value} ms`}
                            tick={{
                                fill: "var(--chart-text)",
                                fontSize: 12
                            }}
                            stroke="var(--chart-text)"
                        >

                            <Label
                                value="Response Time"
                                angle={-90}
                                position="insideLeft"
                                fill="var(--chart-text)"
                                style={{
                                    textAnchor: "middle"
                                }}
                            />

                        </YAxis>

                        <Tooltip content={<CustomTooltip />} />

                        <Line
                            type="monotone"
                            dataKey="responseTime"
                            stroke="#2563eb"
                            strokeWidth={3}
                            dot={{
                                r: 3,
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