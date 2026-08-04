import { useEffect, useState } from "react";

import MainLayout from "../layouts/MainLayout";

import MetricsStatCard from "../components/MetricsComponents/MetricsStatCard";
import EndpointPerformanceTable from "../components/MetricsComponents/EndpointPerformTable";
import HealthTimeline from "../components/MetricsComponents/HealthTimeline";
import HttpStatusSummary from "../components/MetricsComponents/HttpStatusSummary";

import {
    getLatestMetrics,
    getAverageResponseTime,
    getUpCount,
    getDownCount
} from "../services/metricsService";


function Metrics() {

    const [metrics, setMetrics] = useState([]);
    const [averageTime, setAverageTime] = useState(0);
    const [upCount, setUpCount] = useState(0);
    const [downCount, setDownCount] = useState(0);


    useEffect(() => {
        loadMetrics();

        const interval = setInterval(
            loadMetrics,
            30000
        );

        return () => clearInterval(interval);

    }, []);



    const loadMetrics = async () => {

        try {

            const latest = await getLatestMetrics();
            const avg = await getAverageResponseTime();
            const up = await getUpCount();
            const down = await getDownCount();


            setMetrics(latest);
            setAverageTime(avg);
            setUpCount(up);
            setDownCount(down);


        } catch(error) {

            console.error(
                "Metrics loading error:",
                error
            );

        }

    };


    const slowApis = metrics.filter(
        metric => metric.responseTime >= 1000
    ).length;



    return (

        <MainLayout>

            <div className="space-y-8">


                {/* Header */}

                <div>

                    <h1 className="
                        text-3xl
                        font-bold
                        text-slate-900 dark:text-white
                    ">
                        Metrics Analytics
                    </h1>


                    <p className="
                        mt-2
                        text-slate-500 dark:text-slate-400
                    ">
                        Detailed API performance and health insights
                    </p>

                </div>



                {/* Statistics */}

                <div className="
                    grid
                    grid-cols-1
                    md:grid-cols-2
                    xl:grid-cols-4
                    gap-6
                ">


                    <MetricsStatCard
                        title="Healthy APIs"
                        value={upCount}
                        icon="🟢"
                        type="green"
                    />


                    <MetricsStatCard
                        title="Failed APIs"
                        value={downCount}
                        icon="🔴"
                        type="red"
                    />


                    <MetricsStatCard
                        title="Average Response Time"
                        value={`${Number(averageTime || 0).toFixed(0)} ms`}
                        icon="⚡"
                        type="yellow"
                    />


                    <MetricsStatCard
                        title="Slow APIs"
                        value={slowApis}
                        icon="🐌"
                        type="blue"
                    />

                </div>




                {/* Endpoint Detailed Performance */}

                <EndpointPerformanceTable
                    metrics={metrics}
                />


                {/* Timeline + Status */}

                <HealthTimeline
                    metrics={metrics}
                />


                {/* HTTP Status Distribution */}

                <HttpStatusSummary
                    metrics={metrics}
                />



            </div>


        </MainLayout>

    );

}


export default Metrics;