function EndpointPerformTable({ metrics }) {

    const endpointMap = {};

    metrics.forEach(metric => {

        const endpoint = metric.endpointUrl;

        if (!endpointMap[endpoint]) {

            endpointMap[endpoint] = {
                total: 0,
                sum: 0,
                min: metric.responseTime,
                max: metric.responseTime
                , anomaly: metric.anomalous
                , baseline: metric.historicalAverage
                , threshold: metric.anomalyDetectionThreshold
            };

        }

        endpointMap[endpoint].total++;
        endpointMap[endpoint].sum += metric.responseTime;

        endpointMap[endpoint].min =
            Math.min(endpointMap[endpoint].min, metric.responseTime);

        endpointMap[endpoint].max =
            Math.max(endpointMap[endpoint].max, metric.responseTime);

    });


    const data = Object.entries(endpointMap).map(([name, value]) => ({
        name,
        avg: (value.sum / value.total).toFixed(0),
        min: value.min.toFixed(0),
        max: value.max.toFixed(0),
        requests: value.total
        , anomaly: value.anomaly
        , baseline: value.baseline
        , threshold: value.threshold
    }));


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
                text-lg
                font-semibold
                text-slate-900 dark:text-white
                mb-5
            ">
                Endpoint Performance
            </h2>


            <div className="overflow-x-auto">

                <table className="w-full">


                    <thead>

                        <tr className="
                            text-left
                            text-sm
                            text-slate-500 dark:text-slate-400
                            border-b border-slate-200 dark:border-slate-800
                        ">

                            <th className="pb-3">
                                Endpoint
                            </th>

                            <th className="pb-3">
                                Avg
                            </th>

                            <th className="pb-3">
                                Min
                            </th>

                            <th className="pb-3">
                                Max
                            </th>

                            <th className="pb-3">
                                Requests
                            </th>
                            <th className="pb-3">Anomaly</th>

                        </tr>

                    </thead>


                    <tbody>

                    {data.map((item,index)=>(

                        <tr
                            key={index}
                            className="
                                border-b border-slate-200 dark:border-slate-800
                                hover:bg-slate-50 dark:hover:bg-slate-800/60
                                transition
                            "
                        >

                            <td className="
                                py-4
                                text-slate-700 dark:text-slate-200
                                font-medium
                            ">
                                {item.name}
                            </td>


                            <td className="
                                text-slate-700 dark:text-slate-300
                            ">
                                {item.avg} ms
                            </td>


                            <td className="
                                text-slate-700 dark:text-slate-300
                            ">
                                {item.min} ms
                            </td>


                            <td className="
                                text-slate-700 dark:text-slate-300
                            ">
                                {item.max} ms
                            </td>


                            <td className="
                                text-slate-700 dark:text-slate-300
                            ">
                                {item.requests}
                            </td>
                            <td className="text-xs">
                                {item.anomaly ? (
                                    <span className="text-red-500 font-semibold">Detected</span>
                                ) : item.baseline != null ? (
                                    <span className="text-emerald-500">Normal</span>
                                ) : (
                                    <span className="text-slate-400">Building baseline</span>
                                )}
                                {item.baseline != null && <div className="text-slate-400">avg {Number(item.baseline).toFixed(0)} ms</div>}
                            </td>


                        </tr>

                    ))}

                    </tbody>


                </table>

            </div>


        </div>

    );
}

export default EndpointPerformTable;
