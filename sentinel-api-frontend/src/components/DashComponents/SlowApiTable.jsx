import { useState } from "react";

function SlowApiTable({ metrics }) {

    const [showAll, setShowAll] = useState(false);

    const sortedMetrics = [...metrics].sort(
        (a, b) => b.responseTime - a.responseTime
    );

    const displayedMetrics = showAll
        ? sortedMetrics
        : sortedMetrics.slice(0, 6);


    return (
        <div className="
            bg-white dark:bg-slate-900
            rounded-2xl
            shadow-sm
            hover:shadow-md
            border border-slate-200 dark:border-slate-800
            p-6
            transition-all duration-300
        ">


            <div className="flex justify-between items-center mb-6">

                <div>
                    <h2 className="
                        text-lg
                        font-semibold
                        text-slate-800 dark:text-white
                    ">
                        Slow APIs
                    </h2>

                    <p className="
                        text-sm
                        text-slate-500 dark:text-slate-400
                        mt-1
                    ">
                        APIs with highest response latency
                    </p>
                </div>


                {sortedMetrics.length > 6 && (

                    <button
                        onClick={() => setShowAll(!showAll)}
                        className="
                            text-sm
                            font-medium
                            text-blue-600
                            hover:text-blue-800
                            dark:hover:text-blue-400
                        "
                    >
                        {showAll ? "Show Less" : "View More"}
                    </button>

                )}

            </div>



            {displayedMetrics.length === 0 ? (

                <p className="
                    text-slate-500 dark:text-slate-400
                ">
                    No API metrics available.
                </p>

            ) : (

                <div className="overflow-x-auto">

                    <table className="w-full">


                        <thead>

                            <tr className="
                                border-b
                                border-slate-200 dark:border-slate-700
                                text-left
                                text-sm
                                text-slate-500 dark:text-slate-400
                            ">

                                <th className="pb-3">
                                    Sr. No.
                                </th>

                                <th className="pb-3">
                                    Endpoint
                                </th>

                                <th className="pb-3">
                                    Response Time
                                </th>

                                <th className="pb-3">
                                    Status
                                </th>

                                <th className="pb-3">
                                    Checked At
                                </th>

                            </tr>

                        </thead>



                        <tbody>

                            {displayedMetrics.map((metric, index) => (

                                <tr
                                    key={metric.id}
                                    className="
                                        border-b
                                        border-slate-200 dark:border-slate-700
                                        hover:bg-slate-50
                                        dark:hover:bg-slate-800/60
                                        transition
                                    "
                                >

                                    <td className="
                                        py-4
                                        font-medium
                                        text-slate-700 dark:text-slate-200
                                    ">
                                        {index + 1}
                                    </td>



                                    <td className="
                                        py-4
                                        max-w-xs
                                        truncate
                                        text-slate-700 dark:text-slate-200
                                    ">
                                        {metric.endpointUrl}
                                    </td>



                                    <td className="
                                        py-4
                                        font-semibold
                                        text-orange-600
                                        dark:text-orange-400
                                    ">
                                        {metric.responseTime.toFixed(0)} ms
                                    </td>



                                    <td className="py-4">

                                        <span
                                            className={`
                                                px-3 py-1
                                                rounded-full
                                                text-xs
                                                font-semibold

                                                ${
                                                    metric.status === "UP"
                                                    ?
                                                    `
                                                    bg-green-100
                                                    text-green-700
                                                    dark:bg-green-500/20
                                                    dark:text-green-400
                                                    `
                                                    :
                                                    `
                                                    bg-red-100
                                                    text-red-700
                                                    dark:bg-red-500/20
                                                    dark:text-red-400
                                                    `
                                                }
                                            `}
                                        >
                                            {metric.status}
                                        </span>

                                    </td>



                                    <td className="
                                        py-4
                                        text-sm
                                        text-slate-600 dark:text-slate-300
                                    ">
                                        {new Date(
                                            metric.timeStamp
                                        ).toLocaleTimeString()}
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