package vanillax.framework.batch.scheduler;

public enum ScheduleType {
    ONCE{
        @Override
        public String toString() {
            return "Once";
        }
    }, FIXED_RATE{
        @Override
        public String toString() {
            return "Fixed Rate";
        }
    }, FIXED_DELAY{
        @Override
        public String toString() {
            return "Fixed Delay";
        }
    }, CRON{
        @Override
        public String toString() {
            return "Cron Scheduled";
        }
    }
}
