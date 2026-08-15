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
import {
    CircleCheck,
    CircleX,
    Zap,
    Gauge
} from "lucide-react";

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
                error);
        }
    };

    const slowApis = metrics.filter(
        metric => metric.responseTime >= 1000
    ).length;

    return (
        <MainLayout>
            <div className="space-y-8">
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
                        icon={<CircleCheck size={24} strokeWidth={1.8} />}
                        type="green"
                    />

                    <MetricsStatCard
                        title="Failed APIs"
                        value={downCount}
                        icon={<CircleX size={24} strokeWidth={1.8} />}
                        type="red"
                    />

                    <MetricsStatCard
                        title="Average Response Time"
                        value={`${Number(averageTime || 0).toFixed(0)} ms`}
                        icon={<Zap size={24} strokeWidth={1.8} />}
                        type="yellow"
                    />

                    <MetricsStatCard
                        title="Slow APIs"
                        value={slowApis}
                        icon={<Gauge size={24} strokeWidth={1.8} />}
                        type="blue"
                    />

                </div>

                <EndpointPerformanceTable
                    metrics={metrics}
                />
                <HealthTimeline
                    metrics={metrics}
                />
                <HttpStatusSummary
                    metrics={metrics}
                />
            </div>
        </MainLayout>
    );
}

export default Metrics;