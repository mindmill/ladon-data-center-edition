package de.mc.ladon.server.boot.controller.pages

import de.mc.ladon.server.boot.controller.FrameController
import de.mc.ladon.server.boot.tables.Color
import de.mc.ladon.server.boot.tables.TableCell
import de.mc.ladon.server.boot.tables.TableObject
import de.mc.ladon.server.boot.tables.TableRow
import de.mc.ladon.server.core.api.persistence.dao.UserRoleDAO
import de.mc.ladon.server.core.api.persistence.services.LadonUserDetailsManager
import de.mc.ladon.server.core.persistence.entities.impl.LadonUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * UserRolePageController
 * Created by Ralf Ulrich on 13.12.15.
 */
@Controller
class UserRolePageController
@Autowired constructor(
        val userRoleDAO: UserRoleDAO,
        val userDetailsManager: LadonUserDetailsManager) : FrameController() {


    @RequestMapping("users")
    fun users(model: MutableMap<String, Any>,
              @RequestParam repoid: String,
              @RequestParam(required = false) userfilter: String?,
              @RequestParam(required = false) rolefilter: String?
//              @RequestParam(required = false) selectedrole: String?
    ): String {

            model.put("userfilter", userfilter.orEmpty())
            val result = mutableListOf<TableRow>()
            userRoleDAO.getAllUsers { it.name?.contains(userfilter.orEmpty(), true) ?: false }
                    .forEach { user ->
                        val keys = userRoleDAO.getKeysForUser(user.name)
                        result.add(TableRow(listOf(
                                TableCell(user.name),
                                TableCell(user.roles.toString()),
                                TableCell(""),
                                TableCell(""),
                                TableCell("Neuer Key", "genkey?repoid=$repoid&userid=${user.name}")),
                                Color.NONE))

                        result.addAll(keys.map {
                            TableRow(listOf(
                                    TableCell(""),
                                    TableCell(""),
                                    TableCell(it.first),
                                    TableCell(it.second),
                                    TableCell("Löschen", "delkey?repoid=$repoid&accessid=${it.first}")),
                                    Color.NONE)
                        })
                    }
            model.put("users", listOf(TableObject("User", listOf("UserId", "Roles", "AccessKey", "SecretKey"), result)))

        model.put("roles", userRoleDAO.getAllRoleIds { rolefilter?.equals(it) ?: true })
        return super.updateModel(model, "users", repoid)
    }

    @RequestMapping("genkey")
    fun newKey(model: MutableMap<String, Any>,
               @RequestParam repoid: String,
               @RequestParam userid: String): String {
        userRoleDAO.addNewKey(userid)
        return super.updateModel(model, "users", repoid)
    }

    @RequestMapping("delkey")
    fun delKey(model: MutableMap<String, Any>,
               @RequestParam repoid: String,
               @RequestParam accessid: String): String {
        userRoleDAO.deleteKey(accessid)
        return super.updateModel(model, "users", repoid)
    }

    @RequestMapping("createuser")
    fun newUser(model: MutableMap<String, Any>,
                @RequestParam repoid: String,
                @RequestParam userid: String,
                @RequestParam pw: String,
                @RequestParam pw2: String): String {
        if (pw != pw2) {
            model.flashDanger("Passwords don't match")
            return super.updateModel(model, "users", repoid)
        } else {
            userDetailsManager.createUser(LadonUser(userid, BCryptPasswordEncoder().encode(pw), true, setOf("user", "admin")))
            return super.updateModel(model, "users", repoid)
        }
    }

}
