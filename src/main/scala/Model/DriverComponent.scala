package Model

import slick.driver.JdbcProfile

/**
 * Created by xetqL on 12.08.2015.
 */
trait DriverComponent {
    val driver:JdbcProfile
}
