import { useEffect, useState } from "react";
import MainLayout from "../layouts/MainLayout";
import { getAllAlerts } from "../services/alertsService";

function Alerts() {

    const [alerts, setAlerts] = useState([]);
    const [showAll, setShowAll] = useState(false);

    useEffect(() => {
        loadAlerts();
    }, []);

    const loadAlerts = async () => {
        try {
            const data = await getAllAlerts();

            console.log(data);
            console.log(
                data.filter(alert => !alert.resolved)
            );

            setAlerts(data);
        } catch (err) {
            console.error("ALERT ERROR:", err);
        }
    };

    const activeAlerts = alerts.filter(alert => !alert.resolved);

    return (
        <MainLayout>

            <div className="space-y-6">

                <div>
                    <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
                        Alerts
                    </h1>

                    <p className="text-slate-500 dark:text-slate-400 mt-2">
                        Monitor API failures and performance issues
                    </p>
                </div>

                <div className="rounded-2xl p-6 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">

                    <div className="flex justify-between items-center mb-5">

                        <h2 className="text-lg font-semibold text-slate-800 dark:text-white">
                            Active Alerts
                        </h2>

                        <span className="px-3 py-1 rounded-full bg-red-500/10 text-red-500 text-sm">
                            {activeAlerts.length}
                        </span>

                    </div>

                    <div className="space-y-3">

                        {activeAlerts.length === 0 ? (

                            <div className="text-center py-8 text-slate-500 dark:text-slate-400">
                                No active alerts
                            </div>

                        ) : (

                            (showAll ? activeAlerts : activeAlerts.slice(0, 4))
                                .map((alert, index) => (

                                    <div
                                        key={alert.id}
                                        className={`p-4 rounded-xl border ${
                                            alert.severity === "CRITICAL"
                                                ? "bg-red-500/5 border-red-500/20"
                                                : "bg-yellow-500/5 border-yellow-500/20"
                                        }`}
                                    >

                                        <div className="flex justify-between items-center mb-2">

                                            <p className="text-xs text-slate-500 dark:text-slate-400">
                                                ID: {alert.id}
                                            </p>

                                            <span
                                                className={`text-sm font-semibold ${
                                                    alert.severity === "CRITICAL"
                                                        ? "text-red-500"
                                                        : "text-yellow-500"
                                                }`}
                                            >
                                                {alert.severity}
                                            </span>

                                        </div>

                                        <p className="font-medium text-slate-800 dark:text-white">
                                            {alert.endpointUrl}
                                        </p>

                                        <p
                                            className={`text-sm mt-2 ${
                                                alert.severity === "CRITICAL"
                                                    ? "text-red-500"
                                                    : "text-yellow-500"
                                            }`}
                                        >
                                            {alert.message}
                                        </p>

                                        <p className="text-xs text-slate-500 dark:text-slate-400 mt-2">
                                            {alert.type.replace("_", " ")}
                                        </p>

                                    </div>

                                ))

                        )}

                    </div>

                    {activeAlerts.length > 4 && (

                        <div className="flex justify-center mt-5">

                            <button
                                onClick={() => setShowAll(!showAll)}
                                className="px-4 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition"
                            >
                                {showAll ? "View Less" : "View More"}
                            </button>

                        </div>

                    )}

                </div>

            </div>

        </MainLayout>
    );
}

export default Alerts;