import { useState } from "react";
import { useNavigate } from "react-router-dom";

function SlowApiTable({ metrics }) {
    const navigate = useNavigate();
    const [showAll, setShowAll] = useState(false);

    const sortedMetrics = [...metrics].sort(
        (a, b) => b.responseTime - a.responseTime
    );

    const displayedMetrics = sortedMetrics.slice(0, 5);

    const getResponseColor = (time) => {
        if (time < 500) {
            return "text-green-600 dark:text-green-400";
        }

        if (time < 1000) {
            return "text-yellow-600 dark:text-yellow-400";
        }

        return "text-red-600 dark:text-red-400";
    };

    const getStatusStyle = (status) => {
        return status === "UP"
            ? "bg-green-100 text-green-700 dark:bg-green-500/20 dark:text-green-400"
            : "bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-400";
    };

    return (
        <div className="
            bg-white dark:bg-slate-900
            rounded-2xl
            shadow-sm hover:shadow-md
            border border-slate-200 dark:border-slate-800
            p-6
            transition-all duration-300
        ">

            <div className="flex justify-between items-center mb-6">

                <div>

                    <h2 className="
                        text-lg font-semibold
                        text-slate-800 dark:text-white
                    ">
                        Top Slow Endpoints
                    </h2>

                    <p className="
                        mt-1 text-sm
                        text-slate-500 dark:text-slate-400
                    ">
                        Highest response times from the latest health check
                    </p>

                </div>

                {sortedMetrics.length > 5 && (

                    <button
                        onClick={() => navigate("/metrics")}
                        className="
                            text-sm
                            font-medium
                            text-blue-600
                            hover:text-blue-800
                            dark:text-blue-400
                        "
                    >
                        View All Slow APIs →
                    </button>

                )}

            </div>

            {displayedMetrics.length === 0 ? (

                <p className="text-slate-500 dark:text-slate-400">
                    No endpoint metrics available.
                </p>

            ) : (

                <div className="overflow-x-auto">

                    <table className="w-full">

                        <thead>

                            <tr className="
                                border-b border-slate-200 dark:border-slate-700
                                text-left text-sm
                                text-slate-500 dark:text-slate-400
                            ">

                                <th className="pb-3">
                                    Endpoint
                                </th>

                                <th className="pb-3">
                                    Response Time
                                </th>

                                <th className="pb-3">
                                    Status
                                </th>

                            </tr>

                        </thead>

                        <tbody>

                            {displayedMetrics.map((metric) => (

                                <tr
                                    key={metric.id}
                                    className="
                                        border-b border-slate-200 dark:border-slate-700
                                        hover:bg-slate-50 dark:hover:bg-slate-800/60
                                        transition
                                    "
                                >

                                    <td className="
                                        py-4
                                        max-w-xs truncate
                                        font-medium
                                        text-slate-700 dark:text-slate-200
                                    ">
                                        {metric.endpointUrl}
                                    </td>

                                    <td className={`
                                        py-4
                                        font-semibold
                                        ${getResponseColor(metric.responseTime)}
                                    `}>
                                        {metric.responseTime.toFixed(0)} ms
                                    </td>

                                    <td className="py-4">

                                        <span
                                            className={`
                                                px-3 py-1
                                                rounded-full
                                                text-xs
                                                font-semibold
                                                ${getStatusStyle(metric.status)}
                                            `}
                                        >
                                            {metric.status}
                                        </span>

                                    </td>

                                </tr>

                            ))}

                        </tbody>

                    </table>

                </div>

            )}

        </div>
    );
}

export default SlowApiTable;