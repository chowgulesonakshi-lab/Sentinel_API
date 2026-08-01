function StatCard({ title, value, icon, type }) {

    const colors = {
        blue: {
            bg: "bg-blue-500/10",
            text: "text-blue-500"
        },
        green: {
            bg: "bg-green-500/10",
            text: "text-green-500"
        },
        red: {
            bg: "bg-red-500/10",
            text: "text-red-500"
        },
        yellow: {
            bg: "bg-yellow-500/10",
            text: "text-yellow-500"
        }
    };


    return (
        <div className="
            rounded-2xl p-5
            bg-white dark:bg-slate-900
            hover:bg-slate-50 dark:hover:bg-slate-800
            border border-slate-200 dark:border-slate-800
            shadow-sm
            hover:shadow-md
            transition-all duration-300
        ">

            <div className="flex justify-between items-center">

                <div>

                    <p className="
                        text-sm
                        text-slate-500 dark:text-slate-400
                    ">
                        {title}
                    </p>


                    <h2 className="
                        text-3xl
                        font-bold
                        mt-2
                        text-slate-800 dark:text-white
                    ">
                        {value}
                    </h2>

                </div>


                <div className={`
                    w-12 h-12
                    rounded-xl
                    flex items-center justify-center
                    text-xl
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