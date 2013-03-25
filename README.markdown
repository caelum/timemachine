TimeMachine
===========

TimeMachine is a small library created to enable easy testing of code using
[JodaTime](http://joda-time.sourceforge.net/) for date and time representation
and has some logic depending upon getting the current date.

Usage
-----

Suppose you have a ticket that should not be valid three days after its
creation. You could write two tests for that:

    @Test
    public void should_be_valid_after_creation() {
        assertTrue(new Ticket().isValid());
    }

    @Test
    public void should_not_be_valid_three_days_after_creation() {
        Ticket t = TimeMachine
                    .goTo(new DateTime().minusDays(3))
                    .andExecute(new Block<Ticket>() {
                        public Ticket run() {
                            return new Ticket();
                        }
                    });
        assertFalse(t.isValid());
    }

Inside the Block, a new instance of DateTime will be created pointing to three
days ago, instead of pointing to the current date.
