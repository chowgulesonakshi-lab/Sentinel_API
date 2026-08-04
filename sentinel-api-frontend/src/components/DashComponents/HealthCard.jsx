import { useNavigate } from "react-router-dom";
import { PieChart, Pie, Cell, ResponsiveContainer } from "recharts";

function HealthCard({ healthy, down, unknown }) {

    const navigate = useNavigate();

    const data = [
        { name: "Healthy", value: healthy, color: "#22c55e" },
        { name: "Down", value: down, color: "#ef4444" },
        { name: "Unknown", value: unknown, color: "#eab308" }
    ];

    const total = healthy + down + unknown;

    return (
        <div
            className="
                bg-white dark:bg-slate-900
                rounded-2xl
                border border-slate-200 dark:border-slate-800
                shadow-sm hover:shadow-md
                p-6
                transition-all duration-300
            "
        >

            <div className="mb-6">

                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
                    System Health Overview
                </h2>

                <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                    Current health status of monitored endpoints
                </p>

            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">

                {/* Donut Chart */}

                <div className="relative h-52">

                    <ResponsiveContainer width="100%" height="100%">

                        <PieChart>

                            <Pie
                                data={data}
                                dataKey="value"
                                innerRadius={60}
                                outerRadius={85}
                                paddingAngle={4}
                                strokeWidth={0}
                            >

                                {data.map((entry, index) => (

                                    <Cell
                                        key={index}
                                        fill={entry.color}
                                    />

                                ))}

                            </Pie>

                        </PieChart>

                    </ResponsiveContainer>

                    <div className="
                        absolute inset-0
                        flex flex-col items-center justify-center
                    ">

                        <span className="
                            text-3xl font-bold
                            text-slate-900 dark:text-white
                        ">
                            {total}
                        </span>

                        <span className="
                            text-sm
                            text-slate-500 dark:text-slate-400
                        ">
                            Endpoints
                        </span>

                    </div>

                </div>

                {/* Legend */}

                <div className="space-y-4">

                    <div className="flex items-center justify-between">

                        <div className="flex items-center gap-3">

                            <span className="w-3 h-3 rounded-full bg-green-500"></span>

                            <span className="text-slate-700 dark:text-slate-300">
                                Healthy
                            </span>

                        </div>

                        <span className="font-bold text-green-600 dark:text-green-400">
                            {healthy}
                        </span>

                    </div>

                    <div className="flex items-center justify-between">

                        <div className="flex items-center gap-3">

                            <span className="w-3 h-3 rounded-full bg-red-500"></span>

                            <span className="text-slate-700 dark:text-slate-300">
                                Down
                            </span>

                        </div>

                        <span className="font-bold text-red-600 dark:text-red-400">
                            {down}
                        </span>

                    </div>

                    <div className="flex items-center justify-between">

                        <div className="flex items-center gap-3">

                            <span className="w-3 h-3 rounded-full bg-yellow-500"></span>

                            <span className="text-slate-700 dark:text-slate-300">
                                Unknown
                            </span>

                        </div>

                        <span className="font-bold text-yellow-600 dark:text-yellow-400">
                            {unknown}
                        </span>

                    </div>

                </div>

            </div>

            <div className="flex justify-end mt-6">

                <button
                    onClick={() => navigate("/services")}
                    className="
                        text-blue-600 dark:text-blue-400
                        hover:underline
                        font-medium
                    "
                >
                    View All Services →
                </button>

            </div>

        </div>
    );
}

export default HealthCard;