import { useNavigate } from "react-router-dom";

function AlertsCard({ alerts = [] }) {

    const navigate = useNavigate();

    const displayedAlerts = alerts.slice(0, 3);

    return (
        <div className="
            rounded-2xl p-6
            bg-white dark:bg-slate-900
            border border-slate-200 dark:border-slate-800
            shadow-sm
            hover:shadow-md
            transition-all
        ">

            <div className="flex justify-between items-center mb-5">

                <h2 className="
                    text-lg
                    font-semibold
                    text-slate-800 dark:text-white
                ">
                    Recent Critical Alerts
                </h2>


                <span className="
                    px-3 py-1
                    rounded-full
                    bg-red-500/10
                    text-red-500
                    text-sm
                    font-medium
                ">
                    {alerts.length}
                </span>

            </div>


            <div className="space-y-3">

                {displayedAlerts.length === 0 ? (

                    <p className="
                        text-slate-500
                        dark:text-slate-400
                    ">
                        No active alerts
                    </p>

                ) : (

                    displayedAlerts.map((alert) => (

                        <div
                            key={alert.id}
                            className="
                                flex gap-3
                                p-3
                                rounded-xl
                                bg-red-500/5
                                border border-red-500/20
                            "
                        >

                            <span className="text-red-500">
                                🔴
                            </span>


                            <div>

                                <p className="
                                    font-medium
                                    text-slate-800
                                    dark:text-white
                                ">
                                    {alert.type}
                                </p>


                                <p className="
                                    text-sm
                                    text-red-500
                                ">
                                    {alert.message}
                                </p>


                            </div>

                        </div>

                    ))

                )}

            </div>


            <div className="flex justify-end mt-5">

                <button
                    onClick={() => navigate("/alerts")}
                    className="
                        text-blue-600
                        dark:text-blue-400
                        hover:underline
                        transition
                    "
                >
                    View All Alerts →
                </button>

            </div>


        </div>
    );
}

export default AlertsCard;