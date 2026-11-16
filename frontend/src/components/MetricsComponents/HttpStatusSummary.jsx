function HttpStatusSummary({ metrics }) {
    const statusCount = {};
    metrics.forEach(metric => {
        const code = metric.statusCode || "Unknown";
        statusCount[code] =
            (statusCount[code] || 0) + 1;
    });
    const total = metrics.length || 1;

    return (
        <div className="
            bg-white dark:bg-slate-900
            rounded-2xl p-6
            border border-slate-200 dark:border-slate-800
        ">
            <h2 className="
                text-lg font-semibold
                text-slate-900 dark:text-white
                mb-5
            ">
                HTTP Status Summary
            </h2>

            <div className="space-y-4">
                {Object.entries(statusCount).map(([code, count]) => (
                    <div key={code}>
                        <div className="
                            flex justify-between
                            text-sm mb-1
                        ">
                            <span
                                className={`font-medium ${
                                    code >= 500
                                        ? "text-red-500 dark:text-red-400"
                                        : code >= 400
                                        ? "text-yellow-600 dark:text-yellow-400"
                                        : code >= 200 && code < 300
                                        ? "text-emerald-600 dark:text-emerald-400"
                                        : "text-slate-700 dark:text-slate-200"
                                }`}
                            >
                                {code}
                            </span>
                            <span className="
                                text-slate-600
                                dark:text-slate-300
                            ">
                                {Math.round(count / total * 100)}%
                            </span>
                        </div>
                        <div className="
                            h-3
                            bg-slate-200
                            dark:bg-slate-700
                            rounded-full
                        ">
                            <div
                                className="
                                    h-3
                                    bg-blue-500
                                    rounded-full
                                "
                                style={{
                                    width: `${count / total * 100}%`
                                }}
                            />
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default HttpStatusSummary;