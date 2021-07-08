package com.rapid7.armor.write.writers;

import com.rapid7.armor.columnfile.ColumnFileListener;
import com.rapid7.armor.columnfile.ColumnFileReader;
import com.rapid7.armor.columnfile.ColumnFileSection;
import com.rapid7.armor.interval.Interval;
import com.rapid7.armor.io.Compression;
import com.rapid7.armor.meta.ColumnMetadata;
import com.rapid7.armor.schema.ColumnId;
import com.rapid7.armor.schema.DataType;
import com.rapid7.armor.shard.ColumnShardId;
import com.rapid7.armor.shard.ModShardStrategy;
import com.rapid7.armor.shard.ShardId;
import com.rapid7.armor.write.StreamProduct;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColumnFileReaderWriterTest {
   private static List<ColumnId> COLUMNS = Arrays.asList(
      new ColumnId("status", DataType.INTEGER.getCode()),
      new ColumnId("time", DataType.LONG.getCode()),
      new ColumnId("vuln", DataType.STRING.getCode()));


   private static final String TENANT = "test_tenant";
   private static final String TABLE = "test_table";
   private static final Interval INTERVAL = Interval.SINGLE;
   private static final Instant TIMESTAMP = Instant.now();
   private static String TEST_UUID = UUID.randomUUID().toString();
   private static final String ASSET_ID = "assetId";
   private static final Random RANDOM = new Random();

   @Test
   public void testWriteThenRead()
      throws IOException
   {
      ModShardStrategy shardStrategy = new ModShardStrategy(10);
      int entity1Shard = shardStrategy.shardNum(1);
      ColumnId testColumn = new ColumnId("vuln", DataType.STRING.getCode());

      ColumnShardId columnShardId = new ColumnShardId(new ShardId(TENANT, TABLE, INTERVAL.getInterval(), INTERVAL.getIntervalStart(TIMESTAMP), entity1Shard), testColumn);

      ColumnFileWriter cfw = new ColumnFileWriter(columnShardId);
      StreamProduct result = cfw.buildInputStream(Compression.NONE);
      assertNotNull(result);
      InputStream is = result.getInputStream();
      byte[] bytes = ByteStreams.toByteArray(is);
      assertEquals(result.getByteSize(), bytes.length);
      assertTrue(bytes.length > 0);
      System.out.println("size: " + bytes.length);

      DataInputStream str = new DataInputStream(ByteSource.wrap(bytes).openStream());
      ColumnFileReader cfr = new ColumnFileReader();
      ColumnFileListener listener = new ColumnFileListener() {
         @Override public int columnFileSection(
            ColumnFileSection armorSection, ColumnMetadata metadata, DataInputStream inputStream, int compressedLength, int uncompressedLength)
         {
            System.out.println("Got section " + armorSection + " compressed: " + compressedLength + " uncompressed: " + uncompressedLength);
            return 0;
         }
      };
      cfr.read(str, listener);

   }


   @Test
   public void testWriteThenReadV2()
      throws IOException
   {
      ModShardStrategy shardStrategy = new ModShardStrategy(10);
      int entity1Shard = shardStrategy.shardNum(1);
      ColumnId testColumn = new ColumnId("vuln", DataType.STRING.getCode());

      ColumnShardId columnShardId = new ColumnShardId(new ShardId(TENANT, TABLE, INTERVAL.getInterval(), INTERVAL.getIntervalStart(TIMESTAMP), entity1Shard), testColumn);

      ColumnFileWriter cfw = new ColumnFileWriter(columnShardId);
      StreamProduct result = cfw.buildInputStreamV2(Compression.NONE);
      assertNotNull(result);
      InputStream is = result.getInputStream();
      byte[] bytes = ByteStreams.toByteArray(is);
      assertEquals(result.getByteSize(), bytes.length);
      assertTrue(bytes.length > 0);

      DataInputStream str = new DataInputStream(ByteSource.wrap(bytes).openStream());
      ColumnFileReader cfr = new ColumnFileReader();
      ColumnFileListener listener = new ColumnFileListener() {
         @Override public int columnFileSection(
            ColumnFileSection armorSection, ColumnMetadata metadata, DataInputStream inputStream, int compressedLength, int uncompressedLength)
         {
            System.out.println("Got section " + armorSection + " compressed: " + compressedLength + " uncompressed: " + uncompressedLength);
            return 0;
         }
      };
      cfr.read(str, listener);

   }
}
