import { useState } from "react";

function HealthTimeline({ metrics }) {

    const [showAll, setShowAll] = useState(false);


    const timeline = [...metrics]
        .sort(
            (a, b) => new Date(b.timeStamp) - new Date(a.timeStamp)
        );


    const displayedTimeline = showAll
        ? timeline
        : timeline.slice(0, 5);


    return (
        <div className="
            bg-white dark:bg-slate-900
            rounded-2xl p-6
            border border-slate-200 dark:border-slate-800
            shadow-sm
            hover:shadow-md
            transition-all duration-300
        ">

            <h2 className="
                text-lg font-semibold
                text-slate-900 dark:text-white
                mb-5
            ">
                Health Timeline
            </h2>


            <div className="space-y-4">

                {displayedTimeline.map(metric => (

                    <div
                        key={metric.id}
                        className="
                            flex justify-between
                            items-center
                            p-3
                            rounded-xl
                            bg-slate-50
                            dark:bg-slate-800
                        "
                    >

                        <div>
                            <p className="
                                text-sm
                                text-slate-500 dark:text-slate-400
                            ">
                                {new Date(metric.timeStamp)
                                    .toLocaleTimeString()}
                            </p>

                            <p className="
                                font-medium
                                text-slate-900 dark:text-white
                            ">
                                {metric.endpointUrl}
                            </p>
                        </div>


                        <div className="text-right">

                            <p className={
                                metric.success
                                ? "text-green-500 font-semibold"
                                : "text-red-500 font-semibold"
                            }>
                                {metric.success ? "UP" : "DOWN"}
                            </p>

                            <p className="
                                text-sm
                                text-slate-600 dark:text-slate-300
                            ">
                                {metric.responseTime} ms
                            </p>

                        </div>

                    </div>

                ))}

            </div>


            {/* BUTTON MUST BE HERE - OUTSIDE timeline div */}

            {timeline.length > 5 && (

                <div className="flex justify-end mt-5">

                    <button
                        onClick={() => setShowAll(!showAll)}
                        className="
                            text-sm
                            font-medium
                            text-blue-600
                            dark:text-blue-400
                            hover:underline
                        "
                    >
                        {showAll ? "Show Less" : "View More"}
                    </button>

                </div>

            )}

        </div>
    );
}

export default HealthTimeline;