import MainLayout from "../layouts/MainLayout";

function Alerts() {

    const [alerts, setAlerts] = useState([]);

    useEffect(() => {
        loadAlerts();
    }, []);

    const loadAlerts = async () => {
        try {
            const data = await getAlerts();
            setAlerts(data);
        } catch (err) {
            console.error(err);
        }
    };


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


                <div className="rounded-2xl p-6 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-all">

                    <div className="flex justify-between mb-5">

                        <h2 className="text-lg font-semibold text-slate-800 dark:text-white">
                            Active Alerts
                        </h2>

                        <span className="px-3 py-1 rounded-full bg-red-500/10 text-red-500 text-sm">
                            {alerts.length}
                        </span>

                    </div>


                    <div className="space-y-3">

                        {alerts.map((alert, index) => (

                            <div
                                key={index}
                                className="flex gap-3 p-3 rounded-xl bg-red-500/5 border border-red-500/20"
                            >

                                <span className="text-red-500">
                                    ●
                                </span>

                                <div>

                                    <p className="font-medium text-slate-800 dark:text-white">
                                        {alert.name}
                                    </p>

                                    <p className="text-sm text-red-500">
                                        {alert.message}
                                    </p>

                                </div>

                            </div>

                        ))}

                    </div>

                </div>


            </div>

        </MainLayout>
    );
}


export default Alerts;