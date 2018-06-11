/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apress.batch.chapter10.batch;

import java.math.BigDecimal;
import java.util.Date;

import com.apress.batch.chapter10.domain.Account;
import com.apress.batch.chapter10.domain.Customer;
import com.apress.batch.chapter10.domain.Statement;
import com.apress.batch.chapter10.domain.Transaction;

import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.util.CollectionUtils;

/**
 * @author Michael Minella
 */
public class StatementLineAggregator implements LineAggregator<Statement> {

	private static final String ADDRESS_LINE_ONE = "                                                                                                          Apress Banking\n";
	private static final String ADDRESS_LINE_TWO = "                                                                                                   1060 West Addison St.\n";
	private static final String ADDRESS_LINE_THREE = "                                                                                                       Chicago, IL 60613\n\n";
	private static final String STATEMENT_DATE_LINE = "Your Account Summary                                                              Statement Period %tD to %tD\n\n";

//	private static final String SUMMARY_HEADER_FORMAT = "Account Number   %s\n"
//			+ "\nYour Account Summary\n\n";
//	private static final String SUMMARY_FORMAT = "Market Value of Current Securities"
//			+ "                             %s\nCurrent Cash Balance                   "
//			+ "                             %s\nTotal Account Value                     "
//			+ "                       %s\n\n";
//	private static final String CASH_DETAIL_FORMAT = "Account Detail\n\nCash        "
//			+ "                                              %s\n\nSecurities\n\n";
//	private static final String SECURITY_HOLDING_FORMAT = "     %s                  "
//			+ "     %s                           %s\n";
//	private static NumberFormat moneyFormatter = NumberFormat
//			.getCurrencyInstance();

	public String aggregate(Statement statement) {
		StringBuilder output = new StringBuilder();

		formatHeader(statement, output);
		formatAccount(statement, output);

		return output.toString();
	}

	private void formatAccount(Statement statement, StringBuilder output) {
		if(!CollectionUtils.isEmpty(statement.getAccounts())) {
			for (Account account : statement.getAccounts()) {
				System.out.println(">> account last statement date = " + String.format("%tD", account.getLastStatementDate()));
				output.append(String.format(STATEMENT_DATE_LINE, account.getLastStatementDate(), new Date()));

				BigDecimal creditAmount = new BigDecimal(0);
				BigDecimal debitAmount = new BigDecimal(0);
				for (Transaction transaction : account.getTransactions()) {
					if(transaction.getCredit() != null) {
						creditAmount = creditAmount.add(transaction.getCredit());
					}

					if(transaction.getDebit() != null) {
						debitAmount = debitAmount.add(transaction.getDebit());
					}

					output.append(String.format("               %tD          %-50s    %8.2f\n", transaction.getTimestamp(), transaction.getDescription(), transaction.getTransactionAmount()));
				}

				output.append(String.format("                                                                    Total Debit: %14.2f\n", debitAmount));
				output.append(String.format("                                                                    Total Credit: %13.2f\n", creditAmount));
				output.append(String.format("                                                                    Balance: %18.2f\n\n", account.getBalance()));
			}
		}
	}

	private void formatHeader(Statement statement, StringBuilder output) {
		Customer customer = statement.getCustomer();

		String customerName = String.format("\n%s %s", customer.getFirstName(), customer.getLastName());
		output.append(customerName + ADDRESS_LINE_ONE.substring(customerName.length()));

		output.append(customer.getAddress1() + ADDRESS_LINE_TWO.substring(customer.getAddress1().length()));

		String addressString = String.format("%s, %s %s", customer.getCity(), customer.getState(), customer.getPostalCode());
		output.append(addressString + ADDRESS_LINE_THREE.substring(addressString.length()));
	}
}