function StatCard({ title, value, icon, type }) {

    const colors = {
        blue: {
            bg: "bg-blue-500/10",
            text: "text-blue-600 dark:text-blue-400"
        },
        green: {
            bg: "bg-green-500/10",
            text: "text-green-600 dark:text-green-400"
        },
        red: {
            bg: "bg-red-500/10",
            text: "text-red-600 dark:text-red-400"
        },
        yellow: {
            bg: "bg-yellow-500/10",
            text: "text-yellow-600 dark:text-yellow-400"
        }
    };

    return (
        <div className="
            bg-white dark:bg-slate-900
            border border-slate-200 dark:border-slate-800
            rounded-2xl
            p-6
            shadow-sm
            hover:shadow-lg
            hover:-translate-y-1
            transition-all duration-300
        ">

            <div className="flex items-center justify-between">

                <div>

                    <p className="
                        text-xs
                        uppercase
                        tracking-wider
                        font-semibold
                        text-slate-500 dark:text-slate-400
                    ">
                        {title}
                    </p>

                    <h2 className="
                        mt-3
                        text-4xl
                        font-bold
                        text-slate-900 dark:text-white
                    ">
                        {value}
                    </h2>

                </div>

                <div className={`
                    w-14 h-14
                    rounded-2xl
                    flex items-center justify-center
                    text-2xl
                    ${colors[type].bg}
                    ${colors[type].text}
                `}>
                    {icon}
                </div>

            </div>

        </div>
    );
}

export default StatCard;