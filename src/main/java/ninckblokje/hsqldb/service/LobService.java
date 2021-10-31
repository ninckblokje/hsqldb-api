/*
 * Copyright (c) 2021, ninckblokje
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package ninckblokje.hsqldb.service;

import ninckblokje.hsqldb.model.LobCount;
import ninckblokje.hsqldb.util.HSQLDBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class LobService {

    private static final Logger logger = LoggerFactory.getLogger(LobService.class);

    @Inject
    private HSQLDBUtil util;

    public List<LobCount> countLobs() {
        logger.info("Executing countLobs");

        var counts = new ArrayList<LobCount>();

        try (var conn = util.getConnection()) {
            try (var stmt = conn.prepareStatement("select LOB_USAGE_COUNT, count(*), sum(LOB_LENGTH) from SYSTEM_LOBS.LOB_IDS group by LOB_USAGE_COUNT")) {
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        counts.add(new LobCount(
                                rs.getInt(1),
                                rs.getInt(2),
                                rs.getInt(3)
                        ));
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("countLobs SQLException", ex);
            throw new RuntimeException(ex);
        }

        return counts;
    }

    public int deleteUnusedLobs() {
        try (var conn = util.getConnection()) {
            var columnCount = deleteUnusedLobsColumnCount();

            logger.info("Executing deleteUnusedLobs with column count {}", columnCount);

            var query = (columnCount == 2) ?
                    "call SYSTEM_LOBS.DELETE_UNUSED_LOBS(?,?)" : "call SYSTEM_LOBS.DELETE_UNUSED_LOBS(?)";

            try (var stmt = conn.prepareCall(query)) {
                stmt.setLong(1, 9223372036854775807L);
                if (columnCount == 2) {
                    stmt.registerOutParameter(2, Types.INTEGER);
                }

                stmt.execute();

                if (columnCount == 2) {
                    return stmt.getInt(2);
                } else {
                    return -1;
                }
            }
        } catch (SQLException ex) {
            logger.error("deleteUnusedLobs SQLException", ex);
            throw new RuntimeException(ex);
        }
    }

    public void mergeEmptyBlocks() {
        logger.info("Executing mergeEmptyBlocks");

        try (var conn = util.getConnection()) {
            try (var stmt = conn.prepareCall("call SYSTEM_LOBS.MERGE_EMPTY_BLOCKS()")) {
                stmt.execute();
            }
        } catch (SQLException ex) {
            logger.error("mergeEmptyBlocks SQLException", ex);
            throw new RuntimeException(ex);
        }
    }

    int deleteUnusedLobsColumnCount() throws SQLException {
        logger.info("Executing deleteUnusedLobsColumnCount");

        try (var conn = util.getConnection()) {
            try (var stmt = conn.prepareStatement("select count(1) from INFORMATION_SCHEMA.SYSTEM_PROCEDURECOLUMNS where PROCEDURE_NAME = 'DELETE_UNUSED_LOBS'")) {
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }

        logger.error("deleteUnusedLobsColumnCount returned no result");
        throw new RuntimeException("deleteUnusedLobsColumnCount returned no result");
    }
}
