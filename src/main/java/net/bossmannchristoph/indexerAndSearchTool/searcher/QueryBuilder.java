package net.bossmannchristoph.indexerAndSearchTool.searcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryBuilder {

    public static void main(String[] args) throws ParseException {
        Query query = new QueryBuilder().addQuery("my test").addPathFilter("path\\lol haha\\schnauze").build();
        System.out.println(query);
    }
    private final BooleanQuery.Builder builder;

    public static final Logger LOGGER = LogManager.getLogger(QueryBuilder.class.getName());
    public QueryBuilder() {
        builder = new BooleanQuery.Builder();
    }

    public QueryBuilder addQuery(String queryString) throws ParseException {
        QueryParser qp = new QueryParser("contents", new StandardAnalyzer());
        qp.setDefaultOperator(QueryParser.Operator.AND);
        Query query = qp.parse(queryString);
        builder.add(query, BooleanClause.Occur.MUST);
        return this;
    }

    public QueryBuilder addAllDocsQuery() throws ParseException {
        Query query = new MatchAllDocsQuery();
        builder.add(query, BooleanClause.Occur.MUST);
        return this;
    }

    public QueryBuilder addPathFilter(String pathString) throws ParseException {
        Path path = Paths.get(pathString.toLowerCase());

        AtomicInteger index = new AtomicInteger(-1);
        StringBuilder queryStringBuilder = new StringBuilder();
        path.forEach(e -> {
                    int nrPathElement = index.incrementAndGet();
                    queryStringBuilder.append("path_");
                    queryStringBuilder.append(nrPathElement);
                    queryStringBuilder.append(":\"");
                    queryStringBuilder.append(e.toString());
                    queryStringBuilder.append("\"");
                }
        );
        QueryParser qp = new QueryParser("INVALID", new KeywordAnalyzer());
        qp.setDefaultOperator(QueryParser.Operator.AND);
        Query filterQuery = qp.parse(queryStringBuilder.toString());
        builder.add(filterQuery, BooleanClause.Occur.FILTER);
        return this;
    }

    public BooleanQuery build() {
        BooleanQuery query = builder.build();
        LOGGER.debug("Builded query: " + query);
        return query;
    }
}