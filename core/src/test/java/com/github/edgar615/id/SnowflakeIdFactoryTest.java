/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.edgar615.id;

import com.google.common.base.Joiner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by edgar on 16-4-2.
 */
public class SnowflakeIdFactoryTest {

  @Test
  public void testFactorySingleton() throws InterruptedException {
    ExecutorService executorService = Executors.newCachedThreadPool();
    Set<IdFactory> idFactories = new CopyOnWriteArraySet<>();
    int count = 100;
    CountDownLatch countDownLatch = new CountDownLatch(1);
    CountDownLatch countDownLatch2 = new CountDownLatch(count);
    for (int i = 0; i < count; i++) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          try {
            countDownLatch.await();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          idFactories.add(IdFactory.snowflake(1));
          countDownLatch2.countDown();
        }
      });
    }
    countDownLatch.countDown();
    countDownLatch2.await();
    Assert.assertEquals(1, idFactories.size());
  }

  @Test
  public void fetchTime() {
    IdFactory<Long> idFactory = IdFactory.snowflake(100);
    Long id = idFactory.nextId();
    System.out.println(Long.toBinaryString(id).length());
    TimeExtracter timeExtracter = (TimeExtracter) idFactory;
    System.out.println(timeExtracter.fetchTime(id));
    ShardingExtracter shardingExtracter = (ShardingExtracter) idFactory;
    System.out.println(shardingExtracter.fetchSharding(id));
    SeqExtracter seqExtracter = (SeqExtracter) idFactory;
    System.out.println(seqExtracter.fetchSeq(id));
  }

  @Test
  public void testUniqueId() throws InterruptedException {
    ExecutorService executorService = Executors.newCachedThreadPool();
    int threadNum = 100;
    int loopNum = 500;
    Set<Long> ids = new CopyOnWriteArraySet<>();
    for (int j = 0; j < loopNum; j++) {
      CountDownLatch countDownLatch = new CountDownLatch(1);
      CountDownLatch countDownLatch2 = new CountDownLatch(threadNum);
      for (int i = 0; i < threadNum; i++) {
        IdFactory<Long> idFactory = IdFactory.snowflake(i);
        executorService.execute(new Runnable() {
          @Override
          public void run() {
            try {
              countDownLatch.await();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            ids.add(idFactory.nextId());
            countDownLatch2.countDown();
          }
        });
      }
      countDownLatch.countDown();
      countDownLatch2.await();
    }

    Assert.assertEquals(threadNum * loopNum, ids.size());
    System.out.println(Joiner.on("\n").join(ids));

  }

}
